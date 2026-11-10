from django.db import models
from django.utils import timezone

class Post(models.Model):
    author = models.ForeignKey('auth.User', on_delete=models.CASCADE)
    title = models.CharField(max_length=200)
    text = models.TextField()
    created_date = models.DateTimeField(default=timezone.now)
    published_date = models.DateTimeField(blank=True, null=True)
    image = models.ImageField(upload_to='images/%Y/%m/%d/', default='images/default_error.png')

    def publish(self):
        self.published_date = timezone.now()
        self.save()

    def __str__(self):
        return self.title

class Device(models.Model):
    KIND_CHOICES = (('app', '앱'), ('web', '웹'))
    token = models.CharField(max_length=300, unique=True)  # FCM 기기 토큰
    kind = models.CharField(max_length=10, choices=KIND_CHOICES, default='app')
    categories = models.CharField(max_length=100, blank=True, default='person')  # 알림 받을 종류 (person, animal, vehicle, object)
    minutes = models.PositiveIntegerField(default=5)  # 같은 물체는 n분에 한 번만 알림
    enabled = models.BooleanField(default=True)
    created_date = models.DateTimeField(default=timezone.now)

    def __str__(self):
        return self.kind + ' ' + self.token[:20]

class Alert(models.Model):
    device = models.ForeignKey(Device, on_delete=models.CASCADE)
    post = models.ForeignKey(Post, on_delete=models.CASCADE)
    labels = models.CharField(max_length=1000)  # 이번에 알린 물체
    sent_date = models.DateTimeField(default=timezone.now)
    is_sent = models.BooleanField(default=False)
    is_read = models.BooleanField(default=False)

    def __str__(self):
        return self.labels