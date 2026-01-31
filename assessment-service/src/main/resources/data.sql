-- Questions (Assessment Items)

-- JAVA BASICS
INSERT INTO assessment_items (id, domain_id, type, stem, difficulty, is_active, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440301', '550e8400-e29b-41d4-a716-446655440000', 'multiple_choice', 'What is the size of an int in Java?', 0.2, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440302', '550e8400-e29b-41d4-a716-446655440000', 'multiple_choice', 'Which keyword is used to inherit a class?', 0.3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO assessment_item_skills (assessment_item_id, skill_id, weight)
VALUES
('550e8400-e29b-41d4-a716-446655440301', '550e8400-e29b-41d4-a716-446655440010', 1.0), -- Skills: Java Basics
('550e8400-e29b-41d4-a716-446655440302', '550e8400-e29b-41d4-a716-446655440010', 1.0)
ON CONFLICT (assessment_item_id, skill_id) DO NOTHING;

INSERT INTO assessment_item_options (id, assessment_item_id, label, statement, is_correct, feedback_template)
VALUES
-- Q1
('550e8400-e29b-41d4-a716-446655440311', '550e8400-e29b-41d4-a716-446655440301', 'A', '32 bits', true, 'Correct!'),
('550e8400-e29b-41d4-a716-446655440312', '550e8400-e29b-41d4-a716-446655440301', 'B', '64 bits', false, 'Incorrect, that is for long.'),
-- Q2
('550e8400-e29b-41d4-a716-446655440321', '550e8400-e29b-41d4-a716-446655440302', 'A', 'implements', false, 'Incorrect, implements is for interfaces.'),
('550e8400-e29b-41d4-a716-446655440322', '550e8400-e29b-41d4-a716-446655440302', 'B', 'extends', true, 'Correct!')
ON CONFLICT (id) DO NOTHING;

-- REACT BASICS
INSERT INTO assessment_items (id, domain_id, type, stem, difficulty, is_active, created_at, updated_at)
VALUES 
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440100', 'multiple_choice', 'What is used to pass data to components?', 0.3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO assessment_item_skills (assessment_item_id, skill_id, weight)
VALUES
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440110', 1.0) -- Skill: React Basics
ON CONFLICT (assessment_item_id, skill_id) DO NOTHING;

INSERT INTO assessment_item_options (id, assessment_item_id, label, statement, is_correct, feedback_template)
VALUES
-- Q3
('550e8400-e29b-41d4-a716-446655440411', '550e8400-e29b-41d4-a716-446655440401', 'A', 'Props', true, 'Correct!'),
('550e8400-e29b-41d4-a716-446655440412', '550e8400-e29b-41d4-a716-446655440401', 'B', 'State', false, 'State is for internal data.')
ON CONFLICT (id) DO NOTHING;
