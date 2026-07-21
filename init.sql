CREATE TABLE IF NOT EXISTS locations (
    id SERIAL PRIMARY KEY,
    building_name VARCHAR(100) DEFAULT 'อาคารจุฬาภรณวลัยลักษณ์ 1',
    floor INT NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    x_coord NUMERIC(5, 2) NOT NULL,
    y_coord NUMERIC(5, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS issue_reports (
    id SERIAL PRIMARY KEY,
    category VARCHAR(30) NOT NULL DEFAULT 'other',
    description TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO locations (floor, room_number, x_coord, y_coord) VALUES 
(1, '101', 15.00, 20.50),
(1, '102', 35.50, 20.50),
(1, 'Toilet', 80.00, 10.00);