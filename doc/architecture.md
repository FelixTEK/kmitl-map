# สถาปัตยกรรมและการทำงานของระบบ (Architecture & Data Flow)

เอกสารนี้อธิบายภาพรวมว่าแต่ละส่วนของระบบคุยกันอย่างไร **ตามสถานะปัจจุบัน** (อยู่ระหว่างเปลี่ยนจาก Android + Elysia เดิม มาเป็น Next.js `walkwe` เดี่ยว)

## ภาพรวม (สถานะปัจจุบัน)

```
┌──────────────────────┐        SQL        ┌──────────────────┐
│   walkwe (Next.js)     │ ──────────────────▶│   PostgreSQL       │
│  frontend + API routes │◀────────────────────│  (locations,       │
│  (app/api/report, ...) │        rows          │   issue_reports)   │
└──────────────────────┘                      └──────────────────┘

┌─────────────────────┐   HTTP (ไปยัง backend เดิมที่ไม่ได้รันใน compose แล้ว)
│   Android App         │ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─▶  ⚠️ ใช้งานไม่ได้ตอนนี้
│  (Legacy/Frozen)       │
└─────────────────────┘
```

- **`walkwe`** (Next.js) เป็นทั้ง frontend และ backend ในตัวเดียว — React component เรียก API route ของตัวเอง (`app/api/...`) ซึ่งคุยกับ PostgreSQL ตรงผ่าน [`lib/db.js`](../walkwe/lib/db.js) ไม่มี backend แยกอีกต่อไปสำหรับส่วนนี้
- **`android-app`** (Kotlin) เป็น client เดิมที่ยิง HTTP ไปที่ backend เดิม (`src/index.ts`, Bun/Elysia) — แต่ตั้งแต่ `docker-compose.yml` เปลี่ยนไปรัน service `walkwe` แทน service `app` เดิม **backend นั้นไม่ได้ถูกรันโดยอัตโนมัติแล้ว** แอป Android จึงเชื่อมต่อไม่ได้จนกว่าจะมีคนรัน `src/index.ts` แยกเอง (ดู [backend-legacy.md](backend-legacy.md)) — โปรเจกต์นี้ตั้งใจเลิกใช้ android-app แล้ว ไม่ได้อยู่ในแผนแก้ไขต่อ

ทั้งระบบ (`db` + `walkwe`) รันผ่าน Docker Compose เดียวกัน (ดู [walkwe.md](walkwe.md))

## Flow 1: แจ้งปัญหา (Issue Report) — ใช้งานได้จริงแล้ว

ใช้สำหรับแจ้งของหาย, สิ่งอำนวยความสะดวกชำรุด, หรือปัญหาจิปาถะอื่น ๆ ภายในอาคาร

1. ผู้ใช้กรอกฟอร์มใน [`ReportPage.jsx`](../walkwe/components/ReportPage.jsx): หัวเรื่อง, ประเภท (`ทั่วไป`/`อาคาร`/`แจ้งซ่อม`), รายละเอียด, และสถานที่ (คณะ → อาคาร → ชั้น → ห้อง)
2. กด "ยืนยันการยื่นคำร้อง" — ฟอร์มยิง `POST /api/report` พร้อม JSON body แบบ flat object
3. [`app/api/report/route.js`](../walkwe/app/api/report/route.js) validate แล้ว `INSERT` ลงตาราง `issue_reports` จริงผ่าน `lib/db.js` (gen `report_code` แบบ `"RPT-" + Date.now()`)
4. Route ตอบกลับ object `report` ที่ map field กลับให้ตรงกับที่ `ReportPage.jsx` คาดหวัง (`report.id`, `report.location.{...}` ฯลฯ) เพื่อไม่ต้องแก้ frontend
5. ฟอร์มแสดงเลขที่คำร้อง (`report.id` = `report_code`) แล้วเคลียร์ฟอร์ม

รายละเอียด endpoint และ schema เต็มอยู่ใน [walkwe.md](walkwe.md)

## Flow 2: แสดงแผนที่/ตำแหน่งห้อง — ยังไม่ต่อฐานข้อมูลจริง

[`MapView.jsx`](../walkwe/components/MapView.jsx) ยังใช้ข้อมูล node/location บางส่วนแบบ **hardcode จาก `mapConstants.js`** ยังไม่มี `GET /api/locations` ใน `walkwe` ที่ query ตาราง `locations` จริง (มีแผนสร้างใน session ถัดไป) — ตาราง `locations` ในฐานข้อมูลมีอยู่แล้วพร้อมใช้ (schema + seed data ใน `init.sql`) แต่ยังไม่มีโค้ดฝั่ง `walkwe` อ่านมันเลย

## Flow 3 (Legacy/ใช้งานไม่ได้ในปัจจุบัน): Android App ↔ Backend เดิม

เดิมแอป Android เคยเรียก:
- `GET /api/locations` — ดึงตำแหน่งห้อง
- `POST /api/reports` — แจ้งปัญหา (schema แบบเก่า: `category`/`description`)

ทั้งสอง endpoint นี้อยู่ใน `src/index.ts` (Bun + Elysia) ซึ่ง**ไม่ได้อยู่ใน `docker-compose.yml` แล้ว** และ schema ตาราง `issue_reports` ใน `init.sql` ก็เปลี่ยนไปเป็นแบบใหม่ (`report_code`, `subject`, `detail`, ...) ไม่ตรงกับที่ endpoint เดิมคาดหวังอีกต่อไป แอป Android นี้จึง**ใช้งานไม่ได้ในสภาพปัจจุบัน** และไม่มีแผนแก้ไขต่อ (ดู [android-app.md](android-app.md))

## เหตุผลของการออกแบบที่สำคัญ

- **ย้ายมาเป็น Next.js เดี่ยว (`walkwe`)** — รวม frontend + backend ไว้ที่เดียว ลดความซับซ้อนของการ deploy เทียบกับเดิมที่มี Android client + Bun/Elysia backend แยกกัน
- **คง response shape เดิมของ `POST /api/report` ไว้** — เพื่อไม่ต้องแก้ `ReportPage.jsx` เลย แม้ schema ฐานข้อมูลข้างในจะเปลี่ยนไปมาก
- **`report_code` แยกจาก `id` (SERIAL)** — `id` เป็น PK ภายในของ DB ส่วน `report_code` เป็นเลขที่อ้างอิงที่ผู้ใช้เห็น/ใช้ค้นหาได้ ไม่ผูกกับลำดับแถวจริงในตาราง

## ที่ยังไม่มี/ยังไม่เสร็จในระบบ (Out of Scope ปัจจุบัน)

- ไม่มีการยืนยันตัวตนผู้ใช้ (authentication/authorization)
- `GET /api/locations` ใน `walkwe` ยังไม่ถูกสร้าง — `MapView.jsx` ยังใช้ hardcode data
- ไม่มีการอัปเดตสถานะ `issue_reports` (เช่น เปลี่ยนจาก `pending` เป็น `resolved`) จากฝั่ง frontend
- `src/index.ts` (Elysia), `Dockerfile` เดิมที่ root, และ `android-app/` ยังไม่ถูกลบออกจากโปรเจกต์ (รอทดสอบ `walkwe` ผ่านก่อนตามแผน cleanup)
