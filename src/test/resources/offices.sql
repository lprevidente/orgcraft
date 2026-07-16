DELETE FROM offices;

INSERT INTO offices (id, name, address, city, country, creator_id, tenant_id)
VALUES ('77777777-7777-7777-7777-777777777777', 'Rome Office', 'Via Roma 1', 'Rome', 'Italy', '11111111-1111-1111-1111-111111111111', '00000000-0000-0000-0000-000000000001'),
       ('88888888-8888-8888-8888-888888888888', 'Milan Office', 'Via Milano 2', 'Milan', 'Italy', '11111111-1111-1111-1111-111111111111', '00000000-0000-0000-0000-000000000001'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'London Office', 'Baker Street 221', 'London', 'UK', '11111111-1111-1111-1111-111111111111', '00000000-0000-0000-0000-000000000001');
