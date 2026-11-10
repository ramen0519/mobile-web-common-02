import json
import requests
from datetime import timedelta
from django.conf import settings
from django.utils import timezone
from google.oauth2 import service_account
from google.auth.transport.requests import Request
from .models import Device, Alert
from .labels import LABELS, get_category

KO_NAMES = dict(LABELS)
SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']
credentials = None

def get_access_token():
    # 서비스 계정 키로 FCM 접근 토큰 받기 (만료되면 다시 받음)
    global credentials
    if credentials is None:
        credentials = service_account.Credentials.from_service_account_file(settings.FIREBASE_KEY_FILE, scopes=SCOPES)
    if not credentials.valid:
        credentials.refresh(Request())
    return credentials.token

def send_message(token, data):
    access_token = get_access_token()
    url = 'https://fcm.googleapis.com/v1/projects/' + credentials.project_id + '/messages:send'
    headers = {'Authorization': 'Bearer ' + access_token, 'Content-Type': 'application/json; UTF-8'}
    message = {
        'message': {
            'token': token,
            'data': data,
            'android': {'priority': 'high'},
            'webpush': {'headers': {'Urgency': 'high'}},
        }
    }
    return requests.post(url, headers=headers, data=json.dumps(message), timeout=5)

def get_labels(post):
    # YOLO가 보낸 내용 "person, cup," 에서 물체 이름 꺼내기
    names = []
    for name in post.text.split(','):
        name = name.strip()
        if name in KO_NAMES and name not in names:
            names.append(name)
    return names

def send_alert(post):
    labels = get_labels(post)
    if not labels or not settings.FIREBASE_KEY_FILE.exists():
        return

    now = timezone.now()
    for device in Device.objects.filter(enabled=True):
        # 기기에서 고른 종류(사람, 동물, 탈것, 그 외 물건)에 들어가는 물체만
        categories = device.categories.split(',')
        wanted = [name for name in labels if get_category(name) in categories]

        # 같은 물체는 n분에 한 번만
        recent = []
        since = now - timedelta(minutes=device.minutes)
        for alert in Alert.objects.filter(device=device, is_sent=True, sent_date__gte=since):
            recent += alert.labels.split(',')
        names = [name for name in wanted if name not in recent]
        if not names:
            continue

        ko_names = [KO_NAMES[name] for name in names]
        title = '[침입 감지] ' + ko_names[0]
        if len(names) > 1:
            title += ' 외 ' + str(len(names) - 1) + '개'
        data = {
            'post_id': str(post.pk),
            'title': title,
            'body': ', '.join(ko_names) + ' · ' + timezone.localtime(post.created_date).strftime('%H:%M:%S'),
            'image': post.image.url,
        }

        alert = Alert(device=device, post=post, labels=','.join(names), sent_date=now)
        try:
            res = send_message(device.token, data)
            alert.is_sent = res.status_code == 200
            if res.status_code == 404:  # 앱 삭제 등으로 없어진 토큰
                device.enabled = False
                device.save()
            print('알림 전송', device.kind, res.status_code)
        except Exception as e:
            print('알림 전송 실패', e)
        alert.save()
