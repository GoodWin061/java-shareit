INSERT INTO users (id, name, email) VALUES (1, 'User1', 'user1@example.com');
INSERT INTO users (id, name, email) VALUES (2, 'User2', 'user2@example.com');

INSERT INTO items (id, name, description, is_available, owner_id) VALUES (1, 'Item1', 'Desc1', true, 1);
INSERT INTO items (id, name, description, is_available, owner_id) VALUES (2, 'Item2', 'Desc2', true, 1);

INSERT INTO bookings (id, start_date, end_date, item_id, booker_id, status) VALUES (1, '2025-01-01 10:00:00', '2025-12-01 10:00:00', 1, 2, 'WAITING');  -- Future
INSERT INTO bookings (id, start_date, end_date, item_id, booker_id, status) VALUES (2, '2023-01-01 10:00:00', '2023-01-02 10:00:00', 2, 2, 'APPROVED');  -- Past
