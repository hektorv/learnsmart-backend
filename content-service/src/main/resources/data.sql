-- Domains
INSERT INTO domains (id, code, name, description)
VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'BACKEND', 'Backend Development', 'Server-side logic and databases'),
('550e8400-e29b-41d4-a716-446655440100', 'FRONTEND', 'Frontend Development', 'User interfaces and client-side logic')
ON CONFLICT (code) DO NOTHING;

-- Skills
INSERT INTO skills (id, domain_id, code, name, description, level, created_at)
VALUES
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440000', 'JAVA_BASICS', 'Java Basics', 'Core Java syntax and concepts', 'BEGINNER', NOW()),
('550e8400-e29b-41d4-a716-446655440020', '550e8400-e29b-41d4-a716-446655440000', 'SPRING_BOOT', 'Spring Boot', 'Building microservices with Spring Boot', 'INTERMEDIATE', NOW()),
('550e8400-e29b-41d4-a716-446655440110', '550e8400-e29b-41d4-a716-446655440100', 'REACT_BASICS', 'React Basics', 'Components, JSX, and Virtual DOM', 'BEGINNER', NOW()),
('550e8400-e29b-41d4-a716-446655440120', '550e8400-e29b-41d4-a716-446655440100', 'REACT_HOOKS', 'React Hooks', 'Managing state with Hooks', 'INTERMEDIATE', NOW())
ON CONFLICT (domain_id, code) DO NOTHING;

-- Content Items
INSERT INTO content_items (id, domain_id, type, title, description, estimated_minutes, difficulty, is_active, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440000', 'ARTICLE', 'Introduction to Java', 'A comprehensive guide to Java syntax.', 15, 0.2, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440000', 'VIDEO', 'Spring Boot Setup', 'Video tutorial on setting up a Spring Boot project.', 20, 0.4, true, NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440203', '550e8400-e29b-41d4-a716-446655440100', 'ARTICLE', 'React Components 101', 'Understanding functional components.', 10, 0.3, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Map Content to Skills
INSERT INTO content_item_skills (content_item_id, skill_id, weight)
VALUES
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440010', 1.0),
('550e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440020', 1.0),
('550e8400-e29b-41d4-a716-446655440203', '550e8400-e29b-41d4-a716-446655440110', 1.0)
ON CONFLICT (content_item_id, skill_id) DO NOTHING;
