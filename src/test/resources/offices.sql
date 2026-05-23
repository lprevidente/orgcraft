DELETE FROM offices;

INSERT INTO offices (id, name, address, city, country, tenant_id)
VALUES ('77777777-7777-7777-7777-777777777777', 'Rome Office', 'Via Roma 1', 'Rome', 'Italy', 'test-tenant'),
       ('88888888-8888-8888-8888-888888888888', 'Milan Office', 'Via Milano 2', 'Milan', 'Italy', 'test-tenant'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'London Office', 'Baker Street 221', 'London', 'UK', 'test-tenant');
