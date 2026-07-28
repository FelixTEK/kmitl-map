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
    report_code VARCHAR(30) UNIQUE,
    reporter_name VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'ทั่วไป'
        CHECK (type IN ('ทั่วไป', 'อาคาร', 'แจ้งซ่อม')),
    detail TEXT NOT NULL,
    faculty VARCHAR(100),
    building VARCHAR(100),
    floor VARCHAR(20),
    room VARCHAR(20),
    status VARCHAR(20) DEFAULT 'pending'
        CHECK (status IN ('pending', 'in_progress', 'resolved')),
    report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO locations (floor, room_number, x_coord, y_coord) VALUES 
(1, '101', 15.00, 20.50),
(1, '102', 35.50, 20.50),
(1, 'Toilet', 80.00, 10.00);