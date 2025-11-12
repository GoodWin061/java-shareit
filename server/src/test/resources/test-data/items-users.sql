INSERT INTO users (name, email) VALUES ('User1', 'user1@example.com');
INSERT INTO users (name, email) VALUES ('User2', 'user2@example.com');
INSERT INTO items (name, description, is_available, owner_id) VALUES ('Item1', 'Description1', true, 1);
INSERT INTO items (name, description, is_available, owner_id) VALUES ('Item2', 'Description2', true, 2);
INSERT INTO comments (text, author_id, item_id, created) VALUES ('Comment for Item1', 1, 1, '2023-10-01 12:00:00');
INSERT INTO comments (text, author_id, item_id, created) VALUES ('Comment for Item2', 2, 2, '2023-10-02 12:00:00');
INSERT INTO comments (text, author_id, item_id, created) VALUES ('Another Comment for Item1', 1, 1, '2023-10-03 12:00:00');
