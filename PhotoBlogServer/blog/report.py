from datetime import timedelta
from django.core.cache import cache
from django.utils import timezone
from .models import Post
from .labels import LABELS, CATEGORIES, get_category
from .push import get_labels

KO_NAMES = dict(LABELS)

def stats_key():
    return 'report_stats_' + str(timezone.localdate())

def clear_stats():
    # 새 감지가 오면 저장해 둔 통계를 지움
    cache.delete(stats_key())

def make_summary(total, counts, hourly):
    # 오늘 리포트 문장 (예: 감지 7번 (사람 5번, 동물 2번) · 가장 많았던 시간 15시)
    if total == 0:
        return '오늘은 감지된 것이 없습니다.'
    parts = [ko + ' ' + str(counts[name]) + '번' for name, ko, desc in CATEGORIES if counts[name] > 0]
    peak = hourly.index(max(hourly))
    return '감지 ' + str(total) + '번 (' + ', '.join(parts) + ') · 가장 많았던 시간 ' + str(peak) + '시'

def get_stats():
    # 최근 7일 감지 통계 (10분 동안 저장해 두고 씀)
    stats = cache.get(stats_key())
    if stats:
        return stats

    today = timezone.localdate()
    start = today - timedelta(days=6)
    hourly = [0] * 24
    daily = {start + timedelta(days=i): 0 for i in range(7)}
    counts = {name: 0 for name, ko, desc in CATEGORIES}
    objects = {}
    today_total = 0
    today_hourly = [0] * 24
    today_counts = {name: 0 for name, ko, desc in CATEGORIES}

    for post in Post.objects.filter(created_date__date__gte=start):
        labels = get_labels(post)
        created = timezone.localtime(post.created_date)
        if not labels or created.date() not in daily:
            continue  # YOLO 감지가 아닌 글(앱에서 올린 글 등)은 빼고 셈

        hourly[created.hour] += 1
        daily[created.date()] += 1
        for name in labels:
            counts[get_category(name)] += 1
            objects[name] = objects.get(name, 0) + 1

        if created.date() == today:
            today_total += 1
            today_hourly[created.hour] += 1
            for name in labels:
                today_counts[get_category(name)] += 1

    top = sorted(objects.items(), key=lambda item: item[1], reverse=True)[:5]
    total = sum(daily.values())
    stats = {
        'start': str(start),
        'end': str(today),
        'total': total,
        'hourly': hourly,
        'daily': [{'date': day.strftime('%m/%d'), 'count': count} for day, count in daily.items()],
        'categories': [{'name': name, 'ko': ko, 'count': counts[name]} for name, ko, desc in CATEGORIES],
        'top_objects': [{'name': name, 'ko': KO_NAMES[name], 'count': count} for name, count in top],
        'peak_hour': hourly.index(max(hourly)) if total > 0 else None,
        'today': {
            'date': str(today),
            'total': today_total,
            'categories': [{'name': name, 'ko': ko, 'count': today_counts[name]} for name, ko, desc in CATEGORIES],
            'text': make_summary(today_total, today_counts, today_hourly),
        },
    }
    cache.set(stats_key(), stats, 600)
    return stats
