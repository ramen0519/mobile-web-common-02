from blog.models import Post, Device, Alert
from blog.labels import LABELS, CATEGORIES
from rest_framework import serializers

class PostSerializer(serializers.ModelSerializer):
    class Meta:
        model = Post
        fields = ('author', 'title', 'text', 'created_date', 'published_date', 'image')

class DeviceSerializer(serializers.ModelSerializer):
    class Meta:
        model = Device
        fields = ('token', 'kind', 'categories', 'minutes', 'enabled')

    def validate_categories(self, value):
        # 모르는 종류는 빼고 저장
        names = [category[0] for category in CATEGORIES]
        return ','.join([name for name in value.split(',') if name in names])

class AlertSerializer(serializers.ModelSerializer):
    title = serializers.CharField(source='post.title', read_only=True)
    image = serializers.ImageField(source='post.image', read_only=True)
    labels_ko = serializers.SerializerMethodField()

    class Meta:
        model = Alert
        fields = ('id', 'post', 'title', 'image', 'labels', 'labels_ko', 'sent_date', 'is_read')
        read_only_fields = ('post', 'labels', 'sent_date')

    def get_labels_ko(self, alert):
        names = dict(LABELS)
        return ', '.join([names.get(name, name) for name in alert.labels.split(',')])
