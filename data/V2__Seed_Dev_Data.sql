-- Seed data for development environment

-- Insert brunch sessions
INSERT INTO brunch (id, title, require_email, email_regexp) VALUES
('team-planning-2025', 'Team Planning Session - Sprint 25', true, '.*@company\.com'),
('product-review-jan', 'Product Review Brunch - January 2025', false, null),
('tech-architecture', 'Technical Architecture Review', true, '.*@(company\.com|contractor\.org)'),
('weekend-hackathon', 'Weekend Hackathon Ideas', false, null);

-- Insert authentication data for brunch sessions
INSERT INTO brunch_authorization (brunch_id, admin_password_hash, voting_password_hash) VALUES
('team-planning-2025', '{bcrypt}$2b$12$hFIviNUQ8aOPXcm2L.GQue.1moyOSPnpQ63a1bdNBuGa24ESlRye6', '{bcrypt}$2b$12$6lcQf7KnIukwJq7XwEIcDeMTAu6A7OCAjuS.xC2QpIAVT1/3d7mKa'),
('product-review-jan', '{bcrypt}$2b$12$hFIviNUQ8aOPXcm2L.GQue.1moyOSPnpQ63a1bdNBuGa24ESlRye6', '{bcrypt}$2b$12$6lcQf7KnIukwJq7XwEIcDeMTAu6A7OCAjuS.xC2QpIAVT1/3d7mKa'),
('tech-architecture', '{bcrypt}$2b$12$hFIviNUQ8aOPXcm2L.GQue.1moyOSPnpQ63a1bdNBuGa24ESlRye6', '{bcrypt}$2b$12$6lcQf7KnIukwJq7XwEIcDeMTAu6A7OCAjuS.xC2QpIAVT1/3d7mKa'),
('weekend-hackathon', '{bcrypt}$2b$12$hFIviNUQ8aOPXcm2L.GQue.1moyOSPnpQ63a1bdNBuGa24ESlRye6', '{bcrypt}$2b$12$6lcQf7KnIukwJq7XwEIcDeMTAu6A7OCAjuS.xC2QpIAVT1/3d7mKa');

-- Insert questions for Team Planning Session
INSERT INTO question (title, link, min, max, recommended, sort_order_number, brunch_id, optional) VALUES
('Implement user authentication system', 'https://docs.spring.io/spring-security/reference/', 1, 10, 7, 1, 'team-planning-2025', false),
('Migrate to microservices architecture', 'https://microservices.io/', 1, 10, 5, 2, 'team-planning-2025', false),
('Add real-time notifications', null, 1, 10, 8, 3, 'team-planning-2025', false),
('Implement caching layer', 'https://redis.io/docs/', 1, 10, 6, 4, 'team-planning-2025', true),
('Upgrade to Java 24', 'https://openjdk.org/projects/jdk/24/', 1, 10, 4, 5, 'team-planning-2025', true);

-- Insert questions for Product Review
INSERT INTO question (title, link, min, max, recommended, sort_order_number, brunch_id, optional) VALUES
('Launch mobile app MVP', null, 1, 10, 9, 1, 'product-review-jan', false),
('Implement dark mode UI', null, 1, 10, 7, 2, 'product-review-jan', true),
('Add multi-language support', null, 1, 10, 3, 3, 'product-review-jan', true),
('Integrate payment processing', 'https://stripe.com/docs', 1, 10, 8, 4, 'product-review-jan', false),
('Add analytics dashboard', 'https://analytics.google.com/', 1, 10, 6, 5, 'product-review-jan', false);

-- Insert questions for Technical Architecture
INSERT INTO question (title, link, min, max, recommended, sort_order_number, brunch_id, optional) VALUES
('Adopt event-driven architecture', 'https://aws.amazon.com/event-driven-architecture/', 1, 10, 6, 1, 'tech-architecture', false),
('Implement API rate limiting', null, 1, 10, 8, 2, 'tech-architecture', false),
('Add comprehensive monitoring', 'https://prometheus.io/', 1, 10, 9, 3, 'tech-architecture', false),
('Migrate to containerized deployment', 'https://kubernetes.io/', 1, 10, 7, 4, 'tech-architecture', false),
('Implement automated testing pipeline', 'https://docs.github.com/en/actions', 1, 10, 9, 5, 'tech-architecture', false);

-- Insert questions for Weekend Hackathon
INSERT INTO question (title, link, min, max, recommended, sort_order_number, brunch_id, optional) VALUES
('Build AI-powered code reviewer', null, 1, 10, 5, 1, 'weekend-hackathon', true),
('Create developer productivity tracker', null, 1, 10, 7, 2, 'weekend-hackathon', true),
('Implement smart code completion', null, 1, 10, 4, 3, 'weekend-hackathon', true),
('Build automated documentation generator', null, 1, 10, 8, 4, 'weekend-hackathon', true);

-- Insert votes for Team Planning Session
INSERT INTO vote (email, name, brunch_id) VALUES
('alice@company.com', 'Alice Johnson', 'team-planning-2025'),
('bob@company.com', 'Bob Smith', 'team-planning-2025'),
('carol@company.com', 'Carol Wilson', 'team-planning-2025'),
('david@company.com', 'David Brown', 'team-planning-2025');

-- Insert votes for Product Review
INSERT INTO vote (email, name, brunch_id) VALUES
(null, 'Emma Davis', 'product-review-jan'),
(null, 'Frank Miller', 'product-review-jan'),
(null, 'Grace Lee', 'product-review-jan');

-- Insert votes for Technical Architecture
INSERT INTO vote (email, name, brunch_id) VALUES
('john@company.com', 'John Developer', 'tech-architecture'),
('jane@contractor.org', 'Jane Architect', 'tech-architecture'),
('mike@company.com', 'Mike Senior', 'tech-architecture');

-- Insert votes for Weekend Hackathon
INSERT INTO vote (email, name, brunch_id) VALUES
(null, 'Alex Hacker', 'weekend-hackathon'),
(null, 'Sam Coder', 'weekend-hackathon');

-- Insert vote answers for Team Planning Session
-- Alice's votes (voter id 1)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(1, 8, 1),  -- Authentication system
(2, 4, 1),  -- Microservices
(3, 9, 1),  -- Real-time notifications
(4, 6, 1),  -- Caching layer
(5, 3, 1);  -- Java 24 upgrade

-- Bob's votes (voter id 2)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(1, 7, 2),  -- Authentication system
(2, 6, 2),  -- Microservices
(3, 8, 2),  -- Real-time notifications
(4, 7, 2),  -- Caching layer
(5, 5, 2);  -- Java 24 upgrade

-- Carol's votes (voter id 3)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(1, 9, 3),  -- Authentication system
(2, 3, 3),  -- Microservices
(3, 7, 3),  -- Real-time notifications
(4, 5, 3),  -- Caching layer
(5, 2, 3);  -- Java 24 upgrade

-- David's votes (voter id 4)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(1, 6, 4),  -- Authentication system
(2, 7, 4),  -- Microservices
(3, 8, 4),  -- Real-time notifications
(4, 8, 4),  -- Caching layer
(5, 4, 4);  -- Java 24 upgrade

-- Insert vote answers for Product Review
-- Emma's votes (voter id 5)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(6, 9, 5),   -- Mobile app MVP
(7, 8, 5),   -- Dark mode
(8, 2, 5),   -- Multi-language
(9, 10, 5),  -- Payment processing
(10, 7, 5);  -- Analytics dashboard

-- Frank's votes (voter id 6)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(6, 8, 6),   -- Mobile app MVP
(7, 6, 6),   -- Dark mode
(8, 4, 6),   -- Multi-language
(9, 9, 6),   -- Payment processing
(10, 8, 6);  -- Analytics dashboard

-- Grace's votes (voter id 7)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(6, 10, 7),  -- Mobile app MVP
(7, 7, 7),   -- Dark mode
(8, 3, 7),   -- Multi-language
(9, 8, 7),   -- Payment processing
(10, 6, 7);  -- Analytics dashboard

-- Insert vote answers for Technical Architecture
-- John's votes (voter id 8)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(11, 6, 8),  -- Event-driven architecture
(12, 9, 8),  -- API rate limiting
(13, 10, 8), -- Monitoring
(14, 8, 8),  -- Containerized deployment
(15, 9, 8);  -- Automated testing

-- Jane's votes (voter id 9)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(11, 8, 9),  -- Event-driven architecture
(12, 8, 9),  -- API rate limiting
(13, 9, 9),  -- Monitoring
(14, 9, 9),  -- Containerized deployment
(15, 10, 9); -- Automated testing

-- Mike's votes (voter id 10)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(11, 7, 10), -- Event-driven architecture
(12, 7, 10), -- API rate limiting
(13, 8, 10), -- Monitoring
(14, 6, 10), -- Containerized deployment
(15, 8, 10); -- Automated testing

-- Insert vote answers for Weekend Hackathon
-- Alex's votes (voter id 11)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(16, 5, 11), -- AI code reviewer
(17, 8, 11), -- Productivity tracker
(18, 3, 11), -- Smart completion
(19, 9, 11); -- Documentation generator

-- Sam's votes (voter id 12)
INSERT INTO vote_answer (answer_to_id, konfidence_value, vote_id) VALUES
(16, 6, 12), -- AI code reviewer
(17, 7, 12), -- Productivity tracker
(18, 4, 12), -- Smart completion
(19, 8, 12); -- Documentation generator