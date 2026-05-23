DELETE FROM office_assignments;

INSERT INTO office_assignments (id, office_id, user_id, assigned_at, unassigned_at, tenant_id)
VALUES
    -- Mario currently assigned to Rome Office
    ('aaaabbbb-0000-0000-0000-000000000001', '77777777-7777-7777-7777-777777777777',
     '11111111-1111-1111-1111-111111111111', '2024-01-01 09:00:00', null, 'test-tenant'),
    -- Anna: previously Milan (closed), now Rome (active)
    ('aaaabbbb-0000-0000-0000-000000000002', '88888888-8888-8888-8888-888888888888',
     '22222222-2222-2222-2222-222222222222', '2023-06-01 09:00:00', '2024-01-15 09:00:00', 'test-tenant'),
    ('aaaabbbb-0000-0000-0000-000000000003', '77777777-7777-7777-7777-777777777777',
     '22222222-2222-2222-2222-222222222222', '2024-01-15 09:00:00', null, 'test-tenant');
