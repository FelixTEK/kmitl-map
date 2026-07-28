# Backend เดิม (Bun + Elysia) — Legacy

> ⚠️ **เลิกใช้แล้ว**: ตั้งแต่ `docker-compose.yml` เปลี่ยนไปรัน service `walkwe` (Next.js) แทน service `app` เดิม backend นี้**ไม่ได้ถูก build/รันเป็นส่วนหนึ่งของระบบอีกต่อไป** ไฟล์ `src/index.ts` ยังอยู่ในโปรเจกต์เพื่อรอการลบทิ้ง (ดูแผนใน [README.md](../README.md)) เอกสารนี้เก็บไว้อ้างอิงชั่วคราวเท่านั้น สำหรับ backend ที่ใช้งานจริงตอนนี้ดู [walkwe.md](walkwe.md)

ไฟล์หลัก (เดิม): [`src/index.ts`](../src/index.ts)

Backend สร้างด้วย [Elysia](https://elysiajs.com) รันบน [Bun](https://bun.sh) เชื่อมต่อ PostgreSQL ผ่าน `postgres` client โดยอ่าน connection string จาก environment variable `DATABASE_URL`

## Endpoints (เดิม)

### `GET /api/locations`

คืนค่ารายการห้องทั้งหมดจากตาราง `locations`

```json
[
  { "id": 1, "building_name": "อาคารจุฬาภรณวลัยลักษณ์ 1", "floor": 1, "room_number": "101", "x_coord": "15.00", "y_coord": "20.50" }
]
```

### `POST /api/reports`

รับแจ้งปัญหาทั่วไป เช่น ของหาย, สิ่งอำนวยความสะดวกชำรุด บันทึกลงตาราง `issue_reports` เวอร์ชันเดิม (ก่อนเปลี่ยน schema)

Request body:
```json
{ "description": "รายละเอียดปัญหา", "category": "lost_item" }
```

Response:
```json
{ "success": true, "message": "แจ้งปัญหาเรียบร้อย!" }
```

> หมายเหตุ: `init.sql` ปัจจุบันเปลี่ยน schema ตาราง `issue_reports` ไปเป็นแบบที่ [walkwe.md](walkwe.md) ใช้แล้ว (`report_code`, `subject`, `detail`, `faculty`, `building`, ...) endpoint ด้านบนกับ schema ปัจจุบันจึง **ไม่ตรงกันอีกต่อไป** — ถ้ามีใครรัน `src/index.ts` แยกเองจะพังเพราะ column ไม่ตรง

## การรัน (ถ้าจำเป็นต้องรันแยกเพื่อทดสอบ `android-app` เดิม)

```bash
bun install
DATABASE_URL=postgres://admin:password123@localhost:5432/kmitl_map bun run start
```

ต้องมี Postgres รันแยกอยู่แล้ว (เช่นจาก `docker compose up db`) และต้องแก้ schema ให้ตรงกับที่ endpoint นี้คาดหวังเอง เพราะ `init.sql` เปลี่ยนไปแล้ว

## หมายเหตุด้านความปลอดภัย (ยังใช้ได้กับ `.env` ปัจจุบัน)

- `.env` ที่ commit อยู่ในโปรเจกต์มี credentials จริง (`admin`/`password123`) — ควรย้ายออกจาก git และใช้ `.env.example` แทนในโปรเจกต์จริง
- CORS เปิดกว้าง (`cors()` ไม่จำกัด origin) เหมาะสำหรับ dev เท่านั้น
