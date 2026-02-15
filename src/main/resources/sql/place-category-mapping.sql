-- ============================================
-- F&B Primary Type 매핑
-- ============================================

-- 식당 하위 카테고리
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 음식점 (101001)
('restaurant', 101001),
('diner', 101001),
('food', 101001),

-- 한식 (101002)
('korean_restaurant', 101002),

-- 중식 (101003)
('chinese_restaurant', 101003),

-- 일식 (101004)
('japanese_restaurant', 101004),
('sushi_restaurant', 101004),
('ramen_restaurant', 101004),

-- 동남아식 (101005)
('thai_restaurant', 101005),
('vietnamese_restaurant', 101005),
('indonesian_restaurant', 101005),

-- 인도식 (101006)
('indian_restaurant', 101006),

-- 아시안식 (101007)
('asian_restaurant', 101007),

-- 양식 (101008)
('french_restaurant', 101008),
('italian_restaurant', 101008),
('spanish_restaurant', 101008),
('greek_restaurant', 101008),
('american_restaurant', 101008),
('mediterranean_restaurant', 101008),
('pizza_restaurant', 101008),
('hamburger_restaurant', 101008),
('bar_and_grill', 101008),
('sandwich_shop', 101008),
('deli', 101008),

-- 중남미식 (101009)
('mexican_restaurant', 101009),
('brazilian_restaurant', 101009),

-- 중동식 (101010)
('turkish_restaurant', 101010),
('lebanese_restaurant', 101010),
('afghani_restaurant', 101010),
('middle_eastern_restaurant', 101010),

-- 아프리카식 (101011)
('african_restaurant', 101011),

-- 육류 전문 (101012)
('steak_house', 101012),
('barbecue_restaurant', 101012),

-- 해산물 전문 (101013)
('seafood_restaurant', 101013),

-- 채식 (101014)
('vegan_restaurant', 101014),
('vegetarian_restaurant', 101014),

-- 파인다이닝 (101015)
('fine_dining_restaurant', 101015),

-- 패스트푸드 (101016)
('fast_food_restaurant', 101016),

-- 조식 (101017)
('breakfast_restaurant', 101017),
('brunch_restaurant', 101017),

-- 뷔페 (101018)
('buffet_restaurant', 101018),

-- 푸드코트 (101019)
('food_court', 101019),

-- 포장 전문 (101020)
('meal_takeaway', 101020);

-- 주점 (102001)
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES ('bar', 102001),
       ('pub', 102001),
       ('wine_bar', 102001);



-- ============================================
-- 후식 Primary Type 매핑
-- ============================================

-- 카페 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 카페 (201001)
('cafe', 201001),
('internet_cafe', 201001),
('coffee_shop', 201001),

-- 음료 (201002)
('juice_shop', 201002),
('acai_shop', 201002),

-- 찻집 (201003)
('tea_house', 201003),

-- 동물 카페 (201004)
('cat_cafe', 201004),
('dog_cafe', 201004);

-- 디저트 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 제빵 (202001)
('bakery', 202001),
('donut_shop', 202001),
('bagel_shop', 202001),

-- 디저트 전문점 (202002)
('dessert_shop', 202002),
('dessert_restaurant', 202002),

-- 아이스크림 (202003)
('ice_cream_shop', 202003),

-- 제과 (202004)
('confectionery', 202004),
('chocolate_shop', 202004),
('chocolate_factory', 202004),
('candy_store', 202004);



-- ============================================
-- 관광 Primary Type 매핑
-- ============================================

-- 관광명소 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 명소 (301001)
('tourist_attraction', 301001),
('landmark', 301001),
('point_of_interest', 301001),

-- 역사 유적 (301002)
('historical_place', 301002),
('historical_landmark', 301002),

-- 문화 유적 (301003)
('cultural_landmark', 301003),

-- 기념물 (301004)
('monument', 301004),
('sculpture', 301004),

-- 박물관 (301005)
('museum', 301005),

-- 미술관 (301006)
('art_gallery', 301006),
('art_studio', 301006),

-- 안내소 (301007)
('visitor_center', 301007),
('tourist_information_center', 301007),

-- 시청 (301008)
('city_hall', 301008),

-- 묘지 (301009)
('cemetery', 301009);

-- 문화예술 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 공연장 (302001)
('performing_arts_theater', 302001),
('concert_hall', 302001),
('opera_house', 302001),
('philharmonic_hall', 302001),
('auditorium', 302001),
('comedy_club', 302001),

-- 문화시설 (302002)
('cultural_center', 302002),

-- 대학교 (302003)
('university', 302003),

-- 도서관 (302004)
('library', 302004);

-- 공원 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 공원 (303001)
('park', 303001),

-- 놀이터 (303002)
('playground', 303002),

-- 국립 공원 (303003)
('national_park', 303003),
('state_park', 303003),

-- 애견 공원 (303004)
('dog_park', 303004),

-- 휴식공간 (303005)
('picnic_ground', 303005),

-- 전망대 (303006)
('observation_deck', 303006),

-- 광장 (303007)
('plaza', 303007),
('town_square', 303007);

-- 자연 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 해변 (304001)
('beach', 304001),

-- 항구 (304002)
('marina', 304002),

-- 식물원 (304003)
('botanical_garden', 304003),

-- 정원 (304004)
('garden', 304004),

-- 동물원 (304005)
('zoo', 304005),

-- 수족관 (304006)
('aquarium', 304006),

-- 야생동물 (304007)
('wildlife_park', 304007),
('wildlife_refuge', 304007),

-- 천체투영관 (304008)
('planetarium', 304008),

-- 자연 지형 (304009)
('natural_feature', 304009);

-- 종교시설 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 교회 (305001)
('church', 305001),

-- 사원 (305002)
('mosque', 305002),

-- 힌두 사원 (305003)
('hindu_temple', 305003),

-- 교회당 (305004)
('synagogue', 305004),

-- 예배 장소 (305005)
('place_of_worship', 305005);

-- 테마파크 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 놀이공원 (306001)
('amusement_park', 306001),

-- 오락센터 (306002)
('amusement_center', 306002),

-- 워터파크 (306003)
('water_park', 306003),

-- 관람차 (306004)
('ferris_wheel', 306004),

-- 롤러코스터 (306005)
('roller_coaster', 306005);

-- 관람 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 영화 극장 (307001)
('movie_theater', 307001),

-- 야외 극장 (307002)
('amphitheatre', 307002),

-- 경기장 (307003)
('stadium', 307003),
('arena', 307003),
('athletic_field', 307003),
('sports_complex', 307003),

-- 행사장 (307004)
('event_venue', 307004),

-- 컨벤션센터 (307005)
('convention_center', 307005),

-- 연회장 (307006)
('banquet_hall', 307006);

-- 유흥 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 오락실 (308001)
('video_arcade', 308001),

-- 클럽 (308002)
('night_club', 308002),
('dance_hall', 308002),

-- 노래방 (308003)
('karaoke', 308003),

-- 카지노 (308004)
('casino', 308004);



-- ============================================
-- 숙소 Primary Type 매핑
-- ============================================

INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 호텔 (401001)
('hotel', 401001),
('resort_hotel', 401001),
('extended_stay_hotel', 401001),
('motel', 401001),

-- 여관 (401002)
('guest_house', 401002),
('bed_and_breakfast', 401002),
('private_guest_room', 401002),
('hostel', 401002),
('inn', 401002),
('japanese_inn', 401002),
('budget_japanese_inn', 401002),
('farmstay', 401002),
('cottage', 401002),

-- 숙박시설 (401003)
('campground', 401003),
('camping_cabin', 401003),
('rv_park', 401003),
('mobile_home_park', 401003),
('lodging', 401003);



-- ============================================
-- 쇼핑 Primary Type 매핑
-- ============================================

INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 백화점 (501001)
('shopping_mall', 501001),
('department_store', 501001),

-- 마트 (501002)
('supermarket', 501002),
('grocery_store', 501002),
('convenience_store', 501002),
('asian_grocery_store', 501002),
('food_store', 501002),
('butcher_shop', 501002),
('liquor_store', 501002),
('market', 501002),
('warehouse_store', 501002),

-- 의류 (501003)
('clothing_store', 501003),
('shoe_store', 501003),

-- 악세사리 (501004)
('jewelry_store', 501004),

-- 기념품 (501005)
('gift_shop', 501005),

-- 도서 (501006)
('book_store', 501006),

-- 전자제품 (501007)
('electronics_store', 501007),

-- 생활용품 (501008)
('home_goods_store', 501008),

-- 할인점 (501009)
('discount_store', 501009),

-- 스포츠용품점 (501010)
('sporting_goods_store', 501010),

-- 상점 (501011)
('store', 501011),

-- 꽃집 (501012)
('florist', 501012),

-- 드럭스토어 (501013)
('drugstore', 501013),

-- 자동차부품점 (501014)
('auto_parts_store', 501014),

-- 자전거매장 (501015)
('bicycle_store', 501015),

-- 휴대폰매장 (501016)
('cell_phone_store', 501016),

-- 가구매장 (501017)
('furniture_store', 501017),

-- 철물점 (501018)
('hardware_store', 501018),

-- 홈인테리어 (501019)
('home_improvement_store', 501019),

-- 펫숍 (501020)
('pet_store', 501020),

-- 도매상 (501021)
('wholesaler', 501021);



-- ============================================
-- 일반 Primary Type 매핑
-- ============================================

-- 액티비티 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 레저 (901001)
('adventure_sports_center', 901001),

-- 하이킹 (901002)
('hiking_area', 901002),

-- 골프 (901003)
('golf_course', 901003),

-- 체육관 (901004)
('gym', 901004),
('fitness_center', 901004),

-- 스포츠 클럽 (901005)
('sports_club', 901005),

-- 스포츠활동장소 (901006)
('sports_activity_location', 901006),

-- 스포츠클럽 (901007)
('sports_coaching', 901007),

-- 수영장 (901008)
('swimming_pool', 901008),

-- 겨울 레저 (901009)
('ski_resort', 901009),
('ice_skating_rink', 901009),

-- 볼링장 (901010)
('bowling_alley', 901010),

-- 스케이트보드 (901011)
('skateboard_park', 901011),

-- 사이클링 (901012)
('cycling_park', 901012),

-- 오프로딩 지역 (901013)
('off_roading_area', 901013),

-- 낚시 차터 (901014)
('fishing_charter', 901014),

-- 낚시터 (901015)
('fishing_pond', 901015),

-- 마구간 (901016)
('stable', 901016),

-- 어린이캠프 (901017)
('childrens_camp', 901017);

-- 스파 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 사우나 (902001)
('spa', 902001),
('sauna', 902001),
('public_bath', 902001),

-- 마사지 (902002)
('massage', 902002),

-- 힐링 (902003)
('wellness_center', 902003),
('yoga_studio', 902003),

-- 피부과/피부관리 (902004)
('skin_care_clinic', 902004),

-- 태닝스튜디오 (902005)
('tanning_studio', 902005);

-- 교통 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 공항 (903001)
('airport', 903001),
('international_airport', 903001),

-- 기차 (903002)
('train_station', 903002),
('subway_station', 903002),

-- 버스 (903003)
('bus_station', 903003),
('bus_stop', 903003),

-- 배 (903004)
('ferry_terminal', 903004),

-- 택시 (903005)
('taxi_stand', 903005),

-- 주차장 (903006)
('parking', 903006),

-- 휴게소 (903007)
('rest_stop', 903007),

-- 터미널 (903008)
('transit_station', 903008),

-- 주유소 (903009)
('gas_station', 903009),

-- 렌트카 (903010)
('car_rental', 903010),

-- 활주로/소규모비행장 (903011)
('airstrip', 903011),

-- 헬리포트 (903012)
('heliport', 903012),

-- 경전철역 (903013)
('light_rail_station', 903013),

-- 환승주차장 (903014)
('park_and_ride', 903014),

-- 차량기지 (903015)
('transit_depot', 903015),

-- 트럭휴게소 (903016)
('truck_stop', 903016),

-- 전기차충전소 (903017)
('electric_vehicle_charging_station', 903017),

-- 자동차 딜러 (903018)
('car_dealer', 903018),

-- 자동차 정비소 (903019)
('car_repair', 903019),

-- 세차장 (903020)
('car_wash', 903020);

-- 편의시설 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 환전 (904001)
('atm', 904001),
('bank', 904001),

-- 의료 (904002)
('dental_clinic', 904002),
('dentist', 904002),
('doctor', 904002),
('physiotherapist', 904002),
('pharmacy', 904002),
('hospital', 904002),

-- 세탁시설 (904003)
('laundry', 904003),

-- 우체국 (904004)
('post_office', 904004),

-- 여행사 (904005)
('travel_agency', 904005),
('tour_agency', 904005),

-- 동네 (904006)
('neighborhood', 904006),
('establishment', 904006),

-- 경찰서 (904007)
('police', 904007),

-- 공중화장실 (904008)
('public_bathroom', 904008),

-- 커뮤니티센터 (904009)
('community_center', 904009),

-- 소방서 (904010)
('fire_station', 904010);

-- 기타 하위
INSERT INTO place_category_mappings (primary_type, category_id)
VALUES
-- 구내식당 (905001)
('cafeteria', 905001),

-- 배달음식 (905002)
('meal_delivery', 905002),

-- 바베큐장 (905003)
('barbecue_area', 905003),

-- 비디오대여점 (905004)
('movie_rental', 905004),

-- 웨딩장소 (905005)
('wedding_venue', 905005),

-- 기업 사무실 (905006)
('corporate_office', 905006),

-- 농장 (905007)
('farm', 905007),

-- 목장 (905008)
('ranch', 905008),

-- 유치원 (905009)
('preschool', 905009),

-- 초등학교 (905010)
('primary_school', 905010),

-- 학교 (905011)
('school', 905011),

-- 중·고등학교 (905012)
('secondary_school', 905012),

-- 회계사무소 (905013)
('accounting', 905013),

-- 법원 (905014)
('courthouse', 905014),

-- 대사관 (905015)
('embassy', 905015),

-- 관공서 (905016)
('government_office', 905016),

-- 지자체 사무소 (905017)
('local_government_office', 905017),

-- 파출소(일본) (905018)
('neighborhood_police_station', 905018),

-- 카이로프랙터 (905019)
('chiropractor', 905019),

-- 의료연구소 (905020)
('medical_lab', 905020),

-- 아파트 건물 (905021)
('apartment_building', 905021),

-- 아파트 단지 (905022)
('apartment_complex', 905022),

-- 콘도 단지 (905023)
('condominium_complex', 905023),

-- 주거 단지 (905024)
('housing_complex', 905024),

-- 행정구역(1급) (905025)
('administrative_area_level_1', 905025),

-- 행정구역(2급) (905026)
('administrative_area_level_2', 905026),

-- 국가 (905027)
('country', 905027),

-- 도시/마을 (905028)
('locality', 905028),

-- 우편번호 (905029)
('postal_code', 905029),

-- 학군 (905030)
('school_district', 905030),

-- 점성술사 (905031)
('astrologer', 905031),

-- 이발소 (905032)
('barber_shop', 905032),

-- 미용사 (905033)
('beautician', 905033),

-- 뷰티살롱 (905034)
('beauty_salon', 905034),

-- 타투/바디아트 (905035)
('body_art_service', 905035),

-- 케이터링 (905036)
('catering_service', 905036),

-- 아동돌봄 (905037)
('child_care_agency', 905037),

-- 컨설턴트 (905038)
('consultant', 905038),

-- 택배 서비스 (905039)
('courier_service', 905039),

-- 전기기사 (905040)
('electrician', 905040),

-- 음식배달 서비스 (905041)
('food_delivery', 905041),

-- 발 관리 (905042)
('foot_care', 905042),

-- 장례식장 (905043)
('funeral_home', 905043),

-- 헤어케어 (905044)
('hair_care', 905044),

-- 미용실 (905045)
('hair_salon', 905045),

-- 보험대리점 (905046)
('insurance_agency', 905046),

-- 변호사 (905047)
('lawyer', 905047),

-- 열쇠수리 (905048)
('locksmith', 905048),

-- 메이크업 아티스트 (905049)
('makeup_artist', 905049),

-- 이사업체 (905050)
('moving_company', 905050),

-- 네일샵 (905051)
('nail_salon', 905051),

-- 페인트공 (905052)
('painter', 905052),

-- 배관공 (905053)
('plumber', 905053),

-- 점술가 (905054)
('psychic', 905054),

-- 부동산 (905055)
('real_estate_agency', 905055),

-- 지붕공사 (905056)
('roofing_contractor', 905056),

-- 창고/보관 (905057)
('storage', 905057),

-- 썸머캠프 (905058)
('summer_camp_organizer', 905058),

-- 재단사/양복점 (905059)
('tailor', 905059),

-- 통신사 (905060)
('telecommunications_service_provider', 905060),

-- 동물병원 (905061)
('veterinary_care', 905061),

-- 행정구역(3급) (905062)
('administrative_area_level_3', 905062),

-- 행정구역(4급) (905063)
('administrative_area_level_4', 905063),

-- 행정구역(5급) (905064)
('administrative_area_level_5', 905064),

-- 행정구역(6급) (905065)
('administrative_area_level_6', 905065),

-- 행정구역(7급) (905066)
('administrative_area_level_7', 905066),

-- 군도/제도 (905067)
('archipelago', 905067),

-- 통칭 지역명 (905068)
('colloquial_area', 905068),

-- 대륙 (905069)
('continent', 905069),

-- 금융(상위) (905070)
('finance', 905070),

-- 종합건설업체 (905071)
('general_contractor', 905071),

-- 지오코드 (905072)
('geocode', 905072),

-- 건강(상위) (905073)
('health', 905073),

-- 교차로 (905074)
('intersection', 905074),

-- 플러스코드 (905075)
('plus_code', 905075),

-- 정치적 독립체 (905076)
('political', 905076),

-- 우편번호 접두사 (905077)
('postal_code_prefix', 905077),

-- 우편번호 접미사 (905078)
('postal_code_suffix', 905078),

-- 우편 도시 (905079)
('postal_town', 905079),

-- 건물/부지 (905080)
('premise', 905080),

-- 도로/경로 (905081)
('route', 905081),

-- 상세주소 (905082)
('street_address', 905082),

-- 하위지역 (905083)
('sublocality', 905083),

-- 하위지역(1급) (905084)
('sublocality_level_1', 905084),

-- 하위지역(2급) (905085)
('sublocality_level_2', 905085),

-- 하위지역(3급) (905086)
('sublocality_level_3', 905086),

-- 하위지역(4급) (905087)
('sublocality_level_4', 905087),

-- 하위지역(5급) (905088)
('sublocality_level_5', 905088),

-- 건물 내 호수 (905089)
('subpremise', 905089),

--- 기타 (905099)
('etc', 905099)
