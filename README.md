# KMITL Map

ระบบแสดงแผนที่/ตำแหน่งห้องภายในอาคาร (เริ่มต้นที่ "อาคารจุฬาภรณวลัยลักษณ์ 1") พร้อมระบบแจ้งปัญหา (issue report) เช่น ของหาย, สิ่งอำนวยความสะดวกชำรุด, หรือปัญหาจิปาถะอื่น ๆ

> ⚠️ **โปรเจกต์กำลังอยู่ระหว่างเปลี่ยน stack**: จาก `android-app` (Kotlin) + `src/` (Bun/Elysia) เดิม มาเป็น **`walkwe`** (Next.js) ที่ทำหน้าที่ทั้ง frontend และ backend ในตัวเดียว ดูสถานะปัจจุบันของแต่ละส่วนด้านล่าง
>
> **ที่มาของ `walkwe`**: โค้ดในโฟลเดอร์ `walkwe/` มาจากโปรเจกต์ของเพื่อน [pattara-tt/ise-kmitlMap](https://github.com/pattara-tt/ise-kmitlMap) นำมาต่อยอด/รวมเข้ากับ backend ของโปรเจกต์นี้ เพื่อใช้ส่งงานวิชา **05506235 Digital Innovation and Technology Management in Disruptive Era** คณะวิทยาศาสตร์ สาขาวิทยาการคอมพิวเตอร์ สถาบันเทคโนโลยีพระจอมเกล้าเจ้าคุณทหารลาดกระบัง (CS-Sci KMITL)

## ส่วนประกอบของระบบ (สถานะปัจจุบัน)

| ส่วน | สถานะ | รายละเอียด |
|---|---|---|
| **`walkwe/`** (Next.js) | ✅ ใช้งานจริง — เป็น service หลักใน `docker-compose.yml` ตอนนี้ | Frontend + API routes เชื่อมต่อ PostgreSQL โดยตรงผ่าน `lib/db.js` |
| **`src/index.ts`** (Bun + Elysia) | ⚠️ Legacy — ไม่ได้อยู่ใน `docker-compose.yml` แล้ว รอลบทิ้งหลังทดสอบ `walkwe` ผ่าน | เคยเป็น backend เดิมของ `android-app` |
| **`android-app/`** (Kotlin) | ⚠️ Legacy/Frozen — จะเลิกใช้ ไม่มีแผนแก้ไขต่อ | ยิง API ไปที่ backend เดิม (`src/index.ts`) ซึ่งไม่รันใน compose แล้ว จึงใช้งานไม่ได้จนกว่าจะรัน backend เดิมแยกเอง |

ดูรายละเอียดเพิ่มเติมที่:
- [architecture.md](doc/architecture.md) — ภาพรวมสถาปัตยกรรมและการทำงานของระบบ (data flow) ปัจจุบัน
- [walkwe.md](doc/walkwe.md) — Next.js app หลัก (frontend + API + database)
- [backend-legacy.md](doc/backend-legacy.md) — backend เดิม (Bun/Elysia) ที่เลิกใช้แล้ว เก็บไว้อ้างอิงชั่วคราว
- [android-app.md](doc/android-app.md) — แอป Android เดิม (legacy/frozen)

## โครงสร้างโปรเจกต์

```
kmitl-map/
├── walkwe/                   # ⭐ Next.js app หลัก (frontend + backend)
│   ├── app/api/report/       # API route: แจ้งปัญหา (ต่อ PostgreSQL จริงแล้ว)
│   ├── app/api/locations/    # (ยังไม่สร้าง — รอ session ถัดไป)
│   ├── components/           # React components (ReportPage, MapView, ...)
│   ├── lib/db.js             # PostgreSQL client (postgres.js)
│   └── Dockerfile
├── src/
│   └── index.ts              # ⚠️ Legacy: Elysia API server เดิม (ไม่ได้ใช้ใน docker-compose แล้ว)
├── android-app/               # ⚠️ Legacy: Android client (Kotlin) เดิม
├── init.sql                   # DB schema + seed data (locations, issue_reports)
├── migrations/                 # migration scripts สำหรับ DB ที่มีข้อมูลเดิมอยู่แล้ว
├── docker-compose.yml          # Postgres + walkwe container
└── .env                        # DB credentials (local only)
```

## Tech Stack

| ส่วน | เทคโนโลยี |
|---|---|
| Frontend + API | [Next.js](https://nextjs.org) (`walkwe/`) |
| Database | PostgreSQL 17 (Alpine) |
| DB Client | `postgres` (postgres.js) — ใช้ทั้งใน `walkwe` |
| Infra | Docker Compose |
| Legacy Backend | Bun + [Elysia](https://elysiajs.com) (`src/index.ts`) — เลิกใช้ |
| Legacy Mobile | Android (Kotlin, AppCompat) (`android-app/`) — เลิกใช้ |

## Quick Start

```bash
# 1. ตั้งค่า environment variables (ดูตัวอย่างใน .env และ walkwe/.env.local.example)
# 2. รัน Postgres + walkwe ด้วย Docker Compose
docker compose up --build

# walkwe จะพร้อมใช้งานที่ http://localhost:3000
```

> ⚠️ ไฟล์ `.env` ปัจจุบันมี credentials เริ่มต้น (`admin` / `password123`) ซึ่งเหมาะสำหรับ dev เท่านั้น ห้ามใช้ค่านี้ใน production
