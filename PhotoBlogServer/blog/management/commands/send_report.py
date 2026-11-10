from django.conf import settings
from django.core.management.base import BaseCommand
from blog.models import Device
from blog.push import send_message
from blog.report import get_stats

class Command(BaseCommand):
    help = '오늘의 감시 리포트를 리포트 받기를 켠 기기로 보냅니다'

    def handle(self, *args, **options):
        if not settings.FIREBASE_KEY_FILE.exists():
            print('Firebase 키 파일이 없습니다:', settings.FIREBASE_KEY_FILE)
            return

        today = get_stats()['today']
        data = {'type': 'report', 'title': '오늘의 감시 리포트', 'body': today['text']}
        print(today['text'])
        for device in Device.objects.filter(enabled=True, report=True):
            res = send_message(device.token, data)
            print('리포트 전송', device.kind, res.status_code)
