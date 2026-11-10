from django.contrib import admin
from .models import Post, Device, Alert

admin.site.register(Post)

class DeviceAdmin(admin.ModelAdmin):
    list_display = ('kind', 'categories', 'minutes', 'enabled', 'created_date')

class AlertAdmin(admin.ModelAdmin):
    list_display = ('sent_date', 'device', 'post', 'labels', 'is_sent', 'is_read')

admin.site.register(Device, DeviceAdmin)
admin.site.register(Alert, AlertAdmin)
