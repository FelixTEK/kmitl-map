# สถาปัตยกรรมและการทำงานของระบบ (Architecture & Data Flow)

เอกสารนี้อธิบายภาพรวมว่าแต่ละส่วนของระบบคุยกันอย่างไร ตั้งแต่แอป Android ไปจนถึงฐานข้อมูล

## ภาพรวม

```
┌─────────────────────┐        HTTP (JSON)        ┌──────────────────────┐        SQL        ┌──────────────────┐
│   Android App        │ ─────────────────────────▶│   Backend API         │ ──────────────────▶│   PostgreSQL       │
│  (Kotlin, MainActivity│                            │  (Bun + Elysia,       │                     │  (locations,       │
│   / Activity2)        │◀───────────────────────── │   src/index.ts)       │◀────────────────────│   issue_reports)   │
└─────────────────────┘        JSON response        └──────────────────────┘        rows          └──────────────────┘
```

- **Android App** เป็น client ฝั่งผู้ใช้ ยิง HTTP request ไปที่ Backend โดยตรง ไม่มีการเก็บ state หรือ cache ฝั่งแอป
- **Backend API** เป็นตัวกลางเดียวที่คุยกับฐานข้อมูล รับ request จากแอป แปลงเป็น SQL query แล้วส่งผลลัพธ์กลับเป็น JSON
- **PostgreSQL** เก็บข้อมูลสองเรื่องหลัก: ตำแหน่งห้อง (`locations`) และเรื่องแจ้งปัญหา (`issue_reports`)

ทั้งระบบรันผ่าน Docker Compose (ดู [backend.md](backend.md)) ยกเว้น Android App ที่รันแยกบนเครื่อง/emulator ของผู้ใช้

## Flow 1: ดึงข้อมูลตำแหน่งห้อง (Map Data)

1. ผู้ใช้กดปุ่ม "ดึงข้อมูลห้อง อาคารจุฬาภรณวลัยลักษณ์ 1" ใน `MainActivity`
2. แอปสร้าง background thread ยิง `GET /api/locations` ไปที่ Backend (ผ่าน `10.0.2.2:3000` ในกรณีรันบน Emulator)
3. Backend รับ request แล้วรัน `SELECT * FROM locations` บน PostgreSQL
4. Backend ส่งผลลัพธ์กลับเป็น JSON array ของห้องทั้งหมด
5. แอปแสดงผล JSON ดิบใน `tvData` (ยังไม่แปลงเป็นแผนที่ภาพจริง — ดู [android-app.md](android-app.md) ข้อควรทราบ)

## Flow 2: แจ้งปัญหา (Issue Report)

ใช้สำหรับแจ้งของหาย, สิ่งอำนวยความสะดวกชำรุด, หรือปัญหาจิปาถะอื่น ๆ ภายในอาคาร

1. ผู้ใช้เลือกประเภทปัญหาจาก `spinnerCategory` (`lost_item` / `facility` / `other`) และกรอกรายละเอียดใน `etDescription`
2. กดปุ่ม "ส่งแจ้งปัญหา" — แอปสร้าง background thread ยิง `POST /api/reports` พร้อม JSON body `{ description, category }`
3. Backend รับ request แล้ว `INSERT` ลงตาราง `issue_reports` (สถานะเริ่มต้นเป็น `pending`)
4. Backend ตอบกลับ `{ success: true, message: "แจ้งปัญหาเรียบร้อย!" }`
5. แอปแสดงผลลัพธ์และล้างช่องกรอกข้อความหากสำเร็จ

รายละเอียด endpoint และ schema ทั้งหมดอยู่ใน [backend.md](backend.md)

## Flow 3: ทดสอบ WebView (Activity2)

ใช้ตรวจสอบว่า WebView ทำงานได้ปกติในแอป โดยไม่ต้องพึ่งพา backend/เครือข่ายเลย:

1. ผู้ใช้กดปุ่ม "ทดสอบหน้าเว็บ Hello World" ใน `MainActivity`
2. เปิด `Activity2` ซึ่งโหลดไฟล์ static `file:///android_asset/hello.html` ที่ bundle มากับ APK โดยตรง (ไม่มีการเรียกเครือข่าย)

## เหตุผลของการออกแบบที่สำคัญ

- **แอปยิง API ตรงจาก client** — เหมาะกับสเกลปัจจุบัน (โปรเจกต์เรียน/prototype) ยังไม่มี authentication หรือ business logic ฝั่งแอป ทุกอย่างอยู่ที่ backend
- **`10.0.2.2` แทน `localhost`** — เพราะ Android Emulator รัน network namespace แยกจากเครื่อง host การใช้ `localhost` ในแอปจะหมายถึงตัว emulator เอง ไม่ใช่เครื่องที่รัน Docker
- **แยก `issue_reports` ออกจาก `locations`** — เพราะเป็นคนละ domain (ข้อมูลอ้างอิงสถานที่ vs. transactional record ที่ผู้ใช้สร้างขึ้น) และมี lifecycle ต่างกัน (`status: pending → ...`)
- **`category` เป็น optional พร้อม default `'other'`** — ให้ client เก่าที่ไม่ส่ง `category` มายังทำงานได้โดยไม่ error

## ที่ยังไม่มีในระบบ (Out of Scope ปัจจุบัน)

- ไม่มีการยืนยันตัวตนผู้ใช้ (authentication/authorization)
- ไม่มีหน้าจอแสดงแผนที่จริง (แสดงแค่ raw JSON)
- ไม่มีการอัปเดตสถานะ `issue_reports` (เช่น เปลี่ยนจาก `pending` เป็น `resolved`) จากฝั่งแอป
