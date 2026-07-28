# walkwe (Next.js) — App หลักปัจจุบัน

โปรเจกต์: `walkwe/` — Next.js app ที่ทำหน้าที่ทั้ง **frontend** (React components) และ **backend** (App Router API routes) ในตัวเดียว เป็น service หลักที่รันจริงใน [docker-compose.yml](../docker-compose.yml) (service `walkwe`) แทนที่ backend เดิม (ดู [backend-legacy.md](backend-legacy.md))

## โครงสร้างสำคัญ

- [`lib/db.js`](../walkwe/lib/db.js) — PostgreSQL client เดียวที่ทุก API route ใช้ร่วมกัน (`postgres` / postgres.js) อ่าน connection string จาก `DATABASE_URL`
- [`app/api/report/route.js`](../walkwe/app/api/report/route.js) — API แจ้งปัญหา ต่อ PostgreSQL จริงแล้ว (ดู Endpoints ด้านล่าง)
- [`components/ReportPage.jsx`](../walkwe/components/ReportPage.jsx) — ฟอร์มแจ้งปัญหา ยิงไปที่ `/api/report`
- [`components/MapView.jsx`](../walkwe/components/MapView.jsx) — แสดงแผนที่ **ยังใช้ข้อมูลบางส่วนแบบ hardcode จาก `mapConstants.js`** ยังไม่ได้ต่อกับตาราง `locations` ในฐานข้อมูล (ดู "ยังไม่เสร็จ" ด้านล่าง)
- [`.env.local.example`](../walkwe/.env.local.example) — ตัวอย่าง env vars: `ORS_API_KEY`, `TYPHOON_API_KEY`, `DATABASE_URL`
- [`Dockerfile`](../walkwe/Dockerfile) — image สำหรับ build/run ผ่าน `docker-compose.yml`

## Endpoints

### `GET /api/report`

คืนค่ารายการแจ้งปัญหาทั้งหมดจากตาราง `issue_reports` เรียงจากล่าสุดไปเก่าสุด

```sql
SELECT * FROM issue_reports ORDER BY report_date DESC
```

```json
{ "success": true, "reports": [ { "id": 1, "report_code": "RPT-1732000000000", "reporter_name": "...", "subject": "...", "type": "ทั่วไป", "detail": "...", "faculty": "...", "building": "...", "floor": "1", "room": "101", "status": "pending", "report_date": "...", "created_at": "..." } ] }
```

### `POST /api/report`

รับแจ้งปัญหาจากฟอร์ม `ReportPage.jsx` บันทึกลงตาราง `issue_reports` จริง

Request body (มาจาก `ReportPage.jsx` ตรงๆ):
```json
{ "name": "...", "subject": "...", "type": "ทั่วไป", "date": "2026-07-15", "detail": "...", "faculty": "...", "building": "...", "floor": "1", "room": "101" }
```

Field mapping ไปยังตาราง `issue_reports`:

| field จาก client | column ใน DB |
|---|---|
| `name` | `reporter_name` |
| `subject` | `subject` |
| `type` | `type` |
| `date` | `report_date` |
| `detail` | `detail` |
| `faculty` / `building` / `floor` / `room` | ตรงชื่อ column เลย |
| *(gen อัตโนมัติ)* `"RPT-" + Date.now()` | `report_code` |

Response (คง shape เดิมไว้ให้ `ReportPage.jsx` ทำงานได้โดยไม่ต้องแก้):
```json
{ "success": true, "message": "Report submitted successfully.", "report": { "id": "RPT-...", "requester": "...", "subject": "...", "category": "...", "date": "...", "detail": "...", "location": { "faculty": "...", "building": "...", "floor": "...", "room": "..." }, "status": "pending", "createdAt": "..." } }
```

Validation ฝั่ง server: ต้องมี `subject`, `detail` และครบ 4 field ของสถานที่ (`faculty`, `building`, `floor`, `room`) ไม่งั้นตอบ `400`

### `GET /api/locations` — ยังไม่มี

ยังไม่ได้สร้าง endpoint นี้ใน `walkwe` (มีแผนสร้างใน session ถัดไป เพื่อดึงจากตาราง `locations` แทนที่ `mapConstants.js` ที่ hardcode อยู่ใน `MapView.jsx` ตอนนี้)

## Database Schema

ใช้ schema เดียวกับที่กำหนดใน [`init.sql`](../init.sql) ที่ root (รันอัตโนมัติตอนสร้าง Postgres container ครั้งแรกเท่านั้น — ถ้า volume `pgdata` มีอยู่แล้วจะไม่รันซ้ำ)

### `issue_reports`

| Column | Type | Note |
|---|---|---|
| id | SERIAL PK | |
| report_code | VARCHAR(30) UNIQUE | เช่น `RPT-1732000000000` |
| reporter_name | VARCHAR(100) | |
| subject | VARCHAR(255) NOT NULL | |
| type | VARCHAR(30) NOT NULL DEFAULT `'ทั่วไป'` | CHECK IN (`ทั่วไป`, `อาคาร`, `แจ้งซ่อม`) |
| detail | TEXT NOT NULL | |
| faculty | VARCHAR(100) | |
| building | VARCHAR(100) | |
| floor | VARCHAR(20) | |
| room | VARCHAR(20) | |
| status | VARCHAR(20) DEFAULT `'pending'` | CHECK IN (`pending`, `in_progress`, `resolved`) |
| report_date | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | |
| created_at | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | |

### `locations`

| Column | Type | Note |
|---|---|---|
| id | SERIAL PK | |
| building_name | VARCHAR(100) | default `'อาคารจุฬาภรณวลัยลักษณ์ 1'` |
| floor | INT NOT NULL | |
| room_number | VARCHAR(20) NOT NULL | |
| x_coord / y_coord | NUMERIC(5,2) | ตำแหน่งบนแผนที่ |

มีข้อมูลตัวอย่าง (seed) 3 แถว ยังไม่มี API ใน `walkwe` ที่อ่านตารางนี้ (ดูหัวข้อ endpoint `/api/locations` ด้านบน)

## Environment Variables

ตั้งค่าใน `walkwe/.env.local` (คัดลอกจาก [`.env.local.example`](../walkwe/.env.local.example)):

| Variable | ใช้ทำอะไร |
|---|---|
| `DATABASE_URL` | เชื่อมต่อ PostgreSQL — รูปแบบ `postgres://<user>:<password>@<host>:5432/<db_name>` |
| `ORS_API_KEY` | คำนวณเส้นทางเดินผ่าน OpenRouteService (`/api/route`) |
| `TYPHOON_API_KEY` | แชทบอทผู้ช่วยเดิน (`/api/chat`) |

เมื่อรันผ่าน `docker compose up`, `DATABASE_URL` จะถูกตั้งค่าอัตโนมัติจาก `docker-compose.yml` (ชี้ไปที่ service `db` ในเครือข่ายเดียวกัน) ไม่ต้องตั้งเองถ้ารันผ่าน Docker

## การรัน

### ด้วย Docker Compose (แนะนำ)

```bash
docker compose up --build
# walkwe พร้อมใช้งานที่ http://localhost:3000
```

### รันแบบ local (ไม่ผ่าน Docker)

```bash
cd walkwe
npm install
npm run dev
```

ต้องตั้งค่า `DATABASE_URL` ใน `.env.local` เอง และมี Postgres ที่เข้าถึงได้ (เช่นรัน `docker compose up db` แยก)

## สิ่งที่ยังไม่เสร็จ (ต่อจาก session นี้)

- `MapView.jsx` ยังอ่านข้อมูลบางส่วนจาก `mapConstants.js` (hardcode) ไม่ได้ fetch จากตาราง `locations` จริง
- `GET /api/locations` ยังไม่ถูกสร้างใน `walkwe`
- backend เดิม (`src/index.ts`, Bun/Elysia) และแอป `android-app` ยังไม่ถูกลบออกจากโปรเจกต์ (รอทดสอบ `walkwe` ผ่านก่อน)
