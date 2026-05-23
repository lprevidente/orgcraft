-- Remove all users from the database
DELETE
FROM users;

-- Inserire utenti di test con password hashate con BCrypt
-- Le password sono 'Test@1234' per tutti gli utenti

INSERT INTO users (id, first_name, last_name, email, password, tenant_id)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Mario', 'Rossi', 'mario.rossi@example.com', '$2a$12$3FFCsMkpH8QesxnkYC.tIusxgKiap7BvWgEGYbiXdi95oLGgtcK22', 'test-tenant'),
    ('22222222-2222-2222-2222-222222222222', 'Anna', 'Bianchi', 'anna.bianchi@example.com', '$2a$12$3FFCsMkpH8QesxnkYC.tIusxgKiap7BvWgEGYbiXdi95oLGgtcK22', 'test-tenant'),
    ('33333333-3333-3333-3333-333333333333', 'Giuseppe', 'Verdi', 'giuseppe.verdi@example.com', '$2a$12$3FFCsMkpH8QesxnkYC.tIusxgKiap7BvWgEGYbiXdi95oLGgtcK22', 'test-tenant');
