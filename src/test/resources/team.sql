-- Remove all teams from the database
DELETE
FROM teams;

-- Insert test teams
INSERT INTO teams (id, name, created_at, tenant_id)
VALUES ('44444444-4444-4444-4444-444444444444', 'Development Team', '2023-01-01 10:00:00', 'test-tenant'),
       ('55555555-5555-5555-5555-555555555555', 'Marketing Team', '2023-01-02 11:00:00', 'test-tenant'),
       ('66666666-6666-6666-6666-666666666666', 'Sales Team', '2023-01-03 12:00:00', 'test-tenant');
