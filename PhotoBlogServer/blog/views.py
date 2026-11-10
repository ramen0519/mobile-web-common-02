import json
from django.conf import settings
from django.contrib.auth.decorators import login_required
from django.shortcuts import render, get_object_or_404, redirect
from django.utils import timezone
from .models import Post, Device, Alert
from .forms import PostForm
from rest_framework import viewsets, mixins
from rest_framework.authentication import TokenAuthentication, SessionAuthentication
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from .serializers import PostSerializer, DeviceSerializer, AlertSerializer
from .labels import CATEGORIES
from .push import send_alert
from .report import get_stats, clear_stats

def post_list(request):
    posts = Post.objects.filter(
        published_date__lte=timezone.now()
    ).order_by('published_date')
    return render(request, 'blog/post_list.html', {'posts': posts})

def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {'post': post})

def post_new(request):
    if request.method == "POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm()
    return render(request, 'blog/post_edit.html', {'form': form})

def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method == "POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            post = form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form = PostForm(instance=post)
    return render(request, 'blog/post_edit.html', {'form': form})

def js_test(request):
    return render(request, 'blog/js_test.html', {})

class BlogImages(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer

    def perform_create(self, serializer):
        post = serializer.save()
        clear_stats()  # 감시 리포트 통계 다시 계산
        send_alert(post)  # 탐지 알림 보내기

class DeviceViewSet(viewsets.ModelViewSet):
    queryset = Device.objects.all()
    serializer_class = DeviceSerializer
    authentication_classes = [TokenAuthentication, SessionAuthentication]
    permission_classes = [IsAuthenticated]
    lookup_field = 'token'
    lookup_value_regex = '[^/]+'

    def create(self, request):
        # 이미 등록된 기기면 설정만 바꿈 (같은 토큰이 동시에 와도 하나만 만들어짐)
        token = request.data.get('token')
        if not token:
            return Response({'token': ['이 필드는 필수 항목입니다.']}, status=400)
        device, created = Device.objects.get_or_create(token=token)
        serializer = DeviceSerializer(device, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response(serializer.data)

    @action(detail=False)
    def categories(self, request):
        # 알림 설정에서 고르는 종류 4가지
        return Response([{'name': name, 'ko': ko, 'desc': desc} for name, ko, desc in CATEGORIES])

class AlertViewSet(mixins.ListModelMixin, mixins.RetrieveModelMixin, mixins.UpdateModelMixin, viewsets.GenericViewSet):
    serializer_class = AlertSerializer
    authentication_classes = [TokenAuthentication, SessionAuthentication]
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        # 기기 토큰으로 그 기기가 받은 알림만
        token = self.request.query_params.get('token', '')
        return Alert.objects.filter(device__token=token, is_sent=True).order_by('-sent_date')

    @action(detail=False, methods=['post'])
    def read_all(self, request):
        self.get_queryset().update(is_read=True)
        return Response({'result': 'ok'})

class ReportViewSet(viewsets.ViewSet):
    authentication_classes = [TokenAuthentication, SessionAuthentication]
    permission_classes = [IsAuthenticated]

    def list(self, request):
        # 최근 7일 감지 통계
        return Response(get_stats())

    @action(detail=False)
    def today(self, request):
        # 오늘 하루 요약 (앱의 매일 밤 리포트 알림에 씀)
        return Response(get_stats()['today'])

@login_required
def report_page(request):
    stats = get_stats()
    return render(request, 'blog/report.html', {
        'stats': stats,
        'stats_json': json.dumps(stats, ensure_ascii=False),
    })

@login_required
def alert_setting(request):
    return render(request, 'blog/alert_setting.html', {
        'categories': CATEGORIES,
        'firebase_config': json.dumps(settings.FIREBASE_CONFIG),
        'vapid_key': settings.FIREBASE_VAPID_KEY,
    })

def firebase_sw(request):
    # 웹 알림용 서비스 워커 (사이트 맨 위 주소에 있어야 함)
    return render(request, 'blog/firebase-messaging-sw.js', content_type='application/javascript')