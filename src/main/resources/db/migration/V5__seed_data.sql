INSERT INTO regions (name, country, latitude, longitude, area_km2) VALUES
    ('Siechnice',   'Poland', 51.034700, 17.147200,   7.85),
    ('Wroclaw',     'Poland', 51.107883, 17.038538, 292.82),
    ('Opole',       'Poland', 50.675106, 17.921297, 148.99),
    ('Nysa',        'Poland', 50.474440, 17.333330,  28.00),
    ('Klodzko',     'Poland', 50.438100, 16.654200,  24.84),
    ('Glucholazy',  'Poland', 50.308060, 17.373330,  15.00),
    ('Raciborz',    'Poland', 50.091390, 18.218610,  75.00),
    ('Brzeg',       'Poland', 50.862220, 17.468890,  15.80),
    ('Olawa',       'Poland', 50.944440, 17.293890,  18.00),
    ('Jelenia Gora','Poland', 50.900830, 15.729440, 109.21);

INSERT INTO flood_events (region_id, event_type, severity, risk_score, started_at, ended_at, description) VALUES
    (1, 'HISTORICAL', 'CRITICAL',  0.98, '2024-09-13 00:00:00', '2024-09-17 00:00:00',
     'Sept 2024 Central European Flood. Siechnice inundated by Sleza River overflow. ~800 residents evacuated.'),
    (5, 'HISTORICAL', 'CRITICAL',  1.00, '2024-09-13 00:00:00', '2024-09-20 00:00:00',
     'Klodzko Valley flood epicenter. Record water levels on Nysa Klodzka. Catastrophic infrastructure damage.'),
    (4, 'HISTORICAL', 'CRITICAL',  0.97, '2024-09-14 00:00:00', '2024-09-19 00:00:00',
     'Nysa severely flooded. Historical water levels exceeded. Significant property damage.'),
    (6, 'HISTORICAL', 'HIGH',      0.85, '2024-09-14 00:00:00', '2024-09-18 00:00:00',
     'Glucholazy flooded due to Biala Glucholaska overflow.'),
    (2, 'HISTORICAL', 'MODERATE',  0.60, '2024-09-15 00:00:00', '2024-09-17 00:00:00',
     'Wroclaw on high alert. Oder River rising but flood barriers held.'),
    (1, 'PREDICTED',  'LOW',       0.15, '2025-03-01 00:00:00', '2025-03-31 00:00:00',
     'Spring snowmelt prediction — moderate water levels expected, below flood threshold.');

-- Admin user: password = admin123 (BCrypt)
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@climateguard.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');
