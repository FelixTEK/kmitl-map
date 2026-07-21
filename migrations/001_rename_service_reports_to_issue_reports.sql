-- ใช้เฉพาะกับฐานข้อมูลที่ถูกสร้างไปแล้วก่อนเปลี่ยนจาก "แจ้งซ่อม" (service_reports)
-- เป็น "แจ้งปัญหา" (issue_reports) เท่านั้น
-- (ฐานข้อมูลใหม่ที่สร้างจาก init.sql จะมีตาราง issue_reports พร้อม category อยู่แล้ว ไม่ต้องรันไฟล์นี้)

ALTER TABLE IF EXISTS service_reports RENAME TO issue_reports;

ALTER TABLE issue_reports
    ADD COLUMN IF NOT EXISTS category VARCHAR(30) NOT NULL DEFAULT 'other';
