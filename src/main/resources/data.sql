-- 상위 카테고리
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(1, '인문사회', NULL);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(2, '자연과학', NULL);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(3, '공학·기술', NULL);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(4, '경제·경영', NULL);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(5, '예술·문화', NULL);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(6, '스포츠·라이프스타일', NULL);

-- 인문사회 세부
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(7, '철학', 1);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(8, '역사', 1);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(9, '사회학', 1);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(10, '언어', 1);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(11, '심리', 1);

-- 자연과학 세부
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(12, '수학', 2);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(13, '물리', 2);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(14, '화학', 2);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(15, '생물', 2);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(16, '의료', 2);

-- 공학·기술 세부
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(17, 'IT', 3);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(18, 'AI', 3);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(19, '전자', 3);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(20, '기계', 3);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(21, '산업공학', 3);

-- 경제·경영 세부
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(22, '경제', 4);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(23, '비즈니스', 4);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(24, '마케팅', 4);

-- 예술·문화 세부
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(25, '미술', 5);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(26, '음악', 5);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(27, '문학', 5);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(28, 'UI/UX', 5);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(29, '건축', 5);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(30, '영화', 5);

-- 스포츠·라이프스타일 세부
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(31, '건강', 6);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(32, '스포츠', 6);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(33, '여행', 6);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(34, '생활', 6);
INSERT INTO category(category_id, category_name, parent_category_id) VALUES(35, '환경', 6);