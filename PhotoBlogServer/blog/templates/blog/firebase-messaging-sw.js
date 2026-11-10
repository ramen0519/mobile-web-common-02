// 웹 탐지 알림 서비스 워커 (사이트 맨 위 주소에 있어야 함)

// 새 버전이 있으면 바로 교체
self.addEventListener('install', function(event) {
    self.skipWaiting();
});
self.addEventListener('activate', function(event) {
    event.waitUntil(clients.claim());
});

// FCM이 보낸 푸시를 받아 알림 띄우기 (탭을 닫아도 동작)
self.addEventListener('push', function(event) {
    var data = event.data.json().data;
    var options = {body: data.body, data: {url: self.location.origin + '/alert/'}};
    if (data.type == 'report') {
        // 감시 리포트는 사진 없이, 누르면 리포트 페이지
        options.data.url = self.location.origin + '/report/';
    } else {
        options.icon = self.location.origin + data.image;
        options.image = self.location.origin + data.image;
    }
    event.waitUntil(
        self.registration.showNotification(data.title, options).then(function() {
            return clients.matchAll({type: 'window'});
        }).then(function(list) {
            // 알림 페이지가 열려 있으면 기록을 다시 불러오게 알려 줌
            list.forEach(function(client) {
                client.postMessage('new-alert');
            });
        })
    );
});

// 알림을 누르면 알림 페이지 열기
self.addEventListener('notificationclick', function(event) {
    event.notification.close();
    event.waitUntil(clients.openWindow(event.notification.data.url));
});
