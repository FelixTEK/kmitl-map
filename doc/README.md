# KMITL Map

ระบบแสดงแผนที่/ตำแหน่งห้องภายในอาคาร (เริ่มต้นที่ "อาคารจุฬาภรณ์ 1") พร้อมระบบแจ้งปัญหา (issue report) เช่น ของหาย, สิ่งอำนวยความสะดวกชำรุด, หรือปัญหาจิปาถะอื่น ๆ ประกอบด้วย 2 ส่วนหลัก:

- **Backend API** — Bun + Elysia + PostgreSQL (`src/index.ts`)
- **Android App** — Kotlin (`android-app/`) แอปทดสอบเรียกข้อมูลจาก Backend

## โครงสร้างโปรเจกต์

```
kmitl-map/
├── src/
│   └── index.ts            # Elysia API server
├── android-app/             # Android client (Kotlin)
│   └── app/src/main/
│       ├── java/.../MainActivity.kt
│       ├── res/layout/activity_main.xml
│       └── AndroidManifest.xml
├── init.sql                 # DB schema + seed data
├── docker-compose.yml        # Postgres + API container
├── Dockerfile                # Bun API image
├── package.json
└── .env                      # DB credentials (local only)
```

ดูรายละเอียดเพิ่มเติมที่:
- [architecture.md](architecture.md) — ภาพรวมสถาปัตยกรรมและการทำงานของระบบ (data flow)
- [backend.md](backend.md) — API, ฐานข้อมูล, การรันด้วย Docker
- [android-app.md](android-app.md) — แอป Android

## Tech Stack

| ส่วน | เทคโนโลยี |
|---|---|
| API Runtime | [Bun](https://bun.sh) |
| API Framework | [Elysia](https://elysiajs.com) |
| Database | PostgreSQL 17 (Alpine) |
| DB Client | `postgres` (postgres.js) |
| Mobile | Android (Kotlin, AppCompat) |
| Infra | Docker Compose |

## Quick Start

```bash
# 1. ตั้งค่า environment variables (ดูตัวอย่างใน .env)
# 2. รัน Postgres + API ด้วย Docker Compose
docker compose up --build

# API จะพร้อมใช้งานที่ http://localhost:3000
```

> ⚠️ ไฟล์ `.env` ปัจจุบันมี credentials เริ่มต้น (`admin` / `password123`) ซึ่งเหมาะสำหรับ dev เท่านั้น ห้ามใช้ค่านี้ใน production
