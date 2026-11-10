# YOLOv5 (coco) 물체 이름과 한글 이름
LABELS = [
    ('person', '사람'),
    ('bicycle', '자전거'),
    ('car', '자동차'),
    ('motorcycle', '오토바이'),
    ('airplane', '비행기'),
    ('bus', '버스'),
    ('train', '기차'),
    ('truck', '트럭'),
    ('boat', '보트'),
    ('traffic light', '신호등'),
    ('fire hydrant', '소화전'),
    ('stop sign', '정지 표지판'),
    ('parking meter', '주차 요금기'),
    ('bench', '벤치'),
    ('bird', '새'),
    ('cat', '고양이'),
    ('dog', '개'),
    ('horse', '말'),
    ('sheep', '양'),
    ('cow', '소'),
    ('elephant', '코끼리'),
    ('bear', '곰'),
    ('zebra', '얼룩말'),
    ('giraffe', '기린'),
    ('backpack', '배낭'),
    ('umbrella', '우산'),
    ('handbag', '핸드백'),
    ('tie', '넥타이'),
    ('suitcase', '여행 가방'),
    ('frisbee', '원반'),
    ('skis', '스키'),
    ('snowboard', '스노보드'),
    ('sports ball', '공'),
    ('kite', '연'),
    ('baseball bat', '야구 방망이'),
    ('baseball glove', '야구 글러브'),
    ('skateboard', '스케이트보드'),
    ('surfboard', '서핑보드'),
    ('tennis racket', '테니스 라켓'),
    ('bottle', '병'),
    ('wine glass', '와인잔'),
    ('cup', '컵'),
    ('fork', '포크'),
    ('knife', '칼'),
    ('spoon', '숟가락'),
    ('bowl', '그릇'),
    ('banana', '바나나'),
    ('apple', '사과'),
    ('sandwich', '샌드위치'),
    ('orange', '오렌지'),
    ('broccoli', '브로콜리'),
    ('carrot', '당근'),
    ('hot dog', '핫도그'),
    ('pizza', '피자'),
    ('donut', '도넛'),
    ('cake', '케이크'),
    ('chair', '의자'),
    ('couch', '소파'),
    ('potted plant', '화분'),
    ('bed', '침대'),
    ('dining table', '식탁'),
    ('toilet', '변기'),
    ('tv', 'TV'),
    ('laptop', '노트북'),
    ('mouse', '마우스'),
    ('remote', '리모컨'),
    ('keyboard', '키보드'),
    ('cell phone', '휴대폰'),
    ('microwave', '전자레인지'),
    ('oven', '오븐'),
    ('toaster', '토스터'),
    ('sink', '싱크대'),
    ('refrigerator', '냉장고'),
    ('book', '책'),
    ('clock', '시계'),
    ('vase', '꽃병'),
    ('scissors', '가위'),
    ('teddy bear', '곰 인형'),
    ('hair drier', '헤어드라이어'),
    ('toothbrush', '칫솔'),
]

# 알림 설정에서 고르는 종류 (이름, 한글, 설명)
CATEGORIES = [
    ('person', '사람', '사람'),
    ('animal', '동물', '고양이, 개, 새 등'),
    ('vehicle', '탈것', '자동차, 오토바이, 자전거 등'),
    ('object', '그 외 물건', '컵, 병, 의자, 휴대폰 등'),
]

ANIMALS = ['bird', 'cat', 'dog', 'horse', 'sheep', 'cow', 'elephant', 'bear', 'zebra', 'giraffe']
VEHICLES = ['bicycle', 'car', 'motorcycle', 'airplane', 'bus', 'train', 'truck', 'boat']

def get_category(name):
    # 물체 이름이 어느 종류인지
    if name == 'person':
        return 'person'
    if name in ANIMALS:
        return 'animal'
    if name in VEHICLES:
        return 'vehicle'
    return 'object'
