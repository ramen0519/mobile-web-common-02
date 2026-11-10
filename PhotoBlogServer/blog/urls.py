from django.urls import path, include
from rest_framework import routers
from . import views

router = routers.DefaultRouter()
router.register('Post', views.BlogImages)
router.register('Device', views.DeviceViewSet)
router.register('Alert', views.AlertViewSet, basename='alert')

urlpatterns = [
    path('', views.post_list, name='post_list'),
    path('post/<int:pk>/', views.post_detail, name='post_detail'),
    path('post/new/', views.post_new, name='post_new'),
    path('post/<int:pk>/edit/', views.post_edit, name='post_edit'),
    path('js_test/', views.js_test, name='js_test'),
    path('api_root/', include(router.urls)),
    path('alert/', views.alert_setting, name='alert_setting'),
    path('firebase-messaging-sw.js', views.firebase_sw, name='firebase_sw'),
]
