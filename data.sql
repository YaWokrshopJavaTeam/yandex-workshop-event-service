INSERT INTO event (id, name, description, created_date_time, start_date_time, end_date_time, location, owner_id)
VALUES
    (1, 'Test Event 1', 'Description 1', NOW(), '2024-12-01 10:00:00', '2024-12-01 12:00:00', 'Online', 1),
    (2, 'Test Event 2', 'Description 2', NOW(), '2024-12-02 10:00:00', '2024-12-02 12:00:00', 'Offline', 1);
