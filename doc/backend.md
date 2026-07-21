# Backend API

ไฟล์หลัก: [`src/index.ts`](../src/index.ts)

Backend สร้างด้วย [Elysia](https://elysiajs.com) รันบน [Bun](https://bun.sh) เชื่อมต่อ PostgreSQL ผ่าน `postgres` client โดยอ่าน connection string จาก environment variable `DATABASE_URL`

## Endpoints

### `GET /api/locations`

คืนค่ารายการห้องทั้งหมดจากตาราง `locations`

```json
[
  { "id": 1, "building_name": "อาคารจุฬาภรณ์ 1", "floor": 1, "room_number": "101", "x_coord": "15.00", "y_coord": "20.50" }
]
```

### `POST /api/reports`

รับแจ้งปัญหาทั่วไป (ไม่ใช่แค่แจ้งซ่อม) เช่น ของหาย, สิ่งอำนวยความสะดวกชำรุด, หรือปัญหาจิปาถะอื่น ๆ บันทึกลงตาราง `issue_reports`

Request body:
```json
{ "description": "รายละเอียดปัญหา", "category": "lost_item" }
```

`category` เป็น optional (default: `"other"`) ตัวอย่างค่าที่ใช้ได้: `lost_item` (ของหาย), `facility` (สิ่งอำนวยความสะดวกชำรุด), `other` (อื่น ๆ)

Response:
```json
{ "success": true, "message": "แจ้งปัญหาเรียบร้อย!" }
```

## Database Schema

กำหนดไว้ใน [`init.sql`](../init.sql) และถูกรันอัตโนมัติตอนสร้าง Postgres container ครั้งแรก (`docker-entrypoint-initdb.d`) เท่านั้น — ถ้า volume `pgdata` มีอยู่แล้ว `init.sql` จะไม่ถูกรันซ้ำ

### `locations`

| Column | Type | Note |
|---|---|---|
| id | SERIAL PK | |
| building_name | VARCHAR(100) | default `'อาคารจุฬาภรณ์ 1'` |
| floor | INT | |
| room_number | VARCHAR(20) | |
| x_coord | NUMERIC(5,2) | ตำแหน่งบนแผนที่ |
| y_coord | NUMERIC(5,2) | ตำแหน่งบนแผนที่ |

มีข้อมูลตัวอย่าง (seed) 3 แถวสำหรับชั้น 1

### `issue_reports`

ใช้เก็บการแจ้งปัญหาทั่วไป (ของหาย, ปัญหาจิปาถะ ฯลฯ) ไม่ใช่แค่แจ้งซ่อม

| Column | Type | Note |
|---|---|---|
| id | SERIAL PK | |
| category | VARCHAR(30) | default `'other'` เช่น `lost_item`, `facility`, `other` |
| description | TEXT | |
| status | VARCHAR(20) | default `'pending'` |
| created_at | TIMESTAMP | default `CURRENT_TIMESTAMP` |

### Migration สำหรับฐานข้อมูลเดิม

ถ้า Postgres volume ถูกสร้างไปแล้วก่อนที่ตาราง `service_reports` จะถูกเปลี่ยนชื่อเป็น `issue_reports` (และเพิ่มคอลัมน์ `category`) ตาราง `init.sql` จะไม่ถูกรันซ้ำโดยอัตโนมัติ ให้เลือกวิธีใดวิธีหนึ่ง:

1. **รีเซ็ต volume (เฉพาะ dev/ข้อมูลทดสอบ)**
   ```bash
   docker compose down -v
   docker compose up --build
   ```
2. **รัน migration script บน DB เดิมโดยไม่ล้างข้อมูล** — ใช้ [`migrations/001_rename_service_reports_to_issue_reports.sql`](../migrations/001_rename_service_reports_to_issue_reports.sql) ซึ่งจะ `RENAME TABLE` และ `ADD COLUMN IF NOT EXISTS category` ให้:
   ```bash
   docker compose exec -T db psql -U ${DB_USER} -d ${DB_NAME} < migrations/001_rename_service_reports_to_issue_reports.sql
   ```

## การรัน

### ด้วย Docker Compose (แนะนำ)

`docker-compose.yml` นิยาม 2 services:

- **db** — `postgres:17-alpine` เปิด port `5432`, ใช้ `init.sql` เป็น init script, มี healthcheck ด้วย `pg_isready`
- **app** — build จาก `Dockerfile` (`oven/bun:latest`), เปิด port `3000`, รอ `db` healthy ก่อนเริ่ม

Environment variables ที่ต้องมี (ใน `.env`): `DB_USER`, `DB_PASSWORD`, `DB_NAME`

```bash
docker compose up --build
```

### รันแบบ local (ไม่ผ่าน Docker)

ต้องมี Bun และ Postgres ที่เข้าถึงได้ผ่าน `DATABASE_URL`

```bash
bun install
bun run start   # bun run src/index.ts
```

## หมายเหตุด้านความปลอดภัย

- `.env` ที่ commit อยู่ในโปรเจกต์มี credentials จริง (`admin`/`password123`) — ควรย้ายออกจาก git และใช้ `.env.example` แทนในโปรเจกต์จริง
- CORS เปิดกว้าง (`cors()` ไม่จำกัด origin) เหมาะสำหรับ dev เท่านั้น
