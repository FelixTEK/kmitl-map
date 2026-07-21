# Android App

โปรเจกต์: `android-app/` — แอป Android เขียนด้วย Kotlin, package `com.felixtek.kmitlmappilot`

## โครงสร้างสำคัญ

- [`MainActivity.kt`](../android-app/app/src/main/java/com/felixtek/kmitlmappilot/MainActivity.kt) — หน้าจอหลัก
- [`activity_main.xml`](../android-app/app/src/main/res/layout/activity_main.xml) — Layout: ปุ่ม `btnLoadMap`, ปุ่ม `btnOpenActivity2`, ส่วนทดสอบแจ้งปัญหา (`spinnerCategory`, `etDescription`, `btnSendReport`) และ `TextView` (`tvData`) แสดงผลลัพธ์
- [`Activity2.kt`](../android-app/app/src/main/java/com/felixtek/kmitlmappilot/Activity2.kt) — หน้าจอทดสอบ WebView โหลดหน้าเว็บ Hello World
- [`activity_activity2.xml`](../android-app/app/src/main/res/layout/activity_activity2.xml) — Layout ของ Activity2: `WebView` เต็มจอ
- [`assets/hello.html`](../android-app/app/src/main/assets/hello.html) — หน้าเว็บ Hello World ที่ใช้ทดสอบการรัน WebView ในแอป
- [`AndroidManifest.xml`](../android-app/app/src/main/AndroidManifest.xml) — ขอสิทธิ์ `INTERNET` และเปิด `usesCleartextTraffic` เพื่อยิง HTTP (ไม่ใช่ HTTPS) ได้ พร้อมประกาศทั้ง `MainActivity` (launcher) และ `Activity2`

## การทำงาน

เมื่อกดปุ่ม "ดึงข้อมูลห้อง อาคารจุฬาภรณวลัยลักษณ์ 1":

1. แสดงข้อความ "กำลังดึงข้อมูล..."
2. สร้าง background thread เรียก `GET http://10.0.2.2:3000/api/locations`
   - `10.0.2.2` คือ alias ของ Android Emulator ที่ชี้กลับไปยัง `localhost` ของเครื่องที่รัน Docker (backend)
3. แสดงผลลัพธ์ (raw JSON) ใน `tvData` หรือข้อความ error หากเชื่อมต่อไม่สำเร็จ

เมื่อกดปุ่ม "ทดสอบหน้าเว็บ Hello World" จะเปิด `Activity2` ซึ่งแสดง `WebView` ที่โหลดไฟล์ `file:///android_asset/hello.html` (หน้าเว็บ Hello World แบบ static ที่ bundle มากับแอป) ใช้สำหรับทดสอบว่า WebView ทำงานได้ปกติภายในแอป โดยไม่ต้องพึ่งพา backend หรือเครือข่าย

### ทดสอบระบบแจ้งปัญหา (Issue Report)

ส่วนล่างของหน้าจอหลักมี UI สำหรับทดสอบ `POST /api/reports`:

1. เลือกประเภทปัญหาจาก `spinnerCategory`: "ของหาย" (`lost_item`), "สิ่งอำนวยความสะดวกชำรุด" (`facility`), "อื่นๆ" (`other`)
2. กรอกรายละเอียดใน `etDescription`
3. กดปุ่ม "ส่งแจ้งปัญหา" — แอปจะสร้าง background thread ยิง `POST http://10.0.2.2:3000/api/reports` ด้วย JSON body `{ "description": ..., "category": ... }` ผ่าน `HttpURLConnection`
4. แสดงผลลัพธ์ (raw JSON) ใน `tvData` เหมือนกับปุ่มดึงข้อมูลห้อง หากสำเร็จจะล้างช่องกรอกข้อความ

## Build Config

- `compileSdk` / `targetSdk`: 37
- `minSdk`: 29
- Dependencies หลัก: AndroidX (`activity-ktx`, `appcompat`, `constraintlayout`, `core-ktx`), Material Components
- Build ด้วย Gradle (`gradlew` / `gradlew.bat`)

```bash
cd android-app
./gradlew assembleDebug
```

## ข้อควรทราบ

- แอปนี้ยังเป็นเวอร์ชันทดลอง (prototype): แสดง raw JSON เท่านั้น ยังไม่มีการแสดงผลเป็นแผนที่จริง หรือ UI ที่ผ่านการออกแบบสำหรับผู้ใช้จริง
- ต้องรัน backend (ดู [backend.md](backend.md)) ผ่าน Docker บนเครื่องเดียวกับที่รัน Emulator ก่อนกดปุ่มดึงข้อมูล

## Troubleshooting: Build ไม่ผ่าน (jlink executable does not exist)

ถ้า sync/build ใน Android Studio ล้มเหลวด้วย error ประมาณ `jlink executable ... does not exist` สาเหตุคือ Android Studio auto-detect เจอ JRE ตัวอื่นในเครื่อง (เช่น JRE ที่ฝังมากับ extension ของ VS Code) ซึ่งเป็น JRE แบบตัดทอนที่ไม่มี `jlink.exe` มาใช้แทน JBR ของ Android Studio เอง

วิธีแก้ที่ใช้ในโปรเจกต์นี้:
1. กำหนด `org.gradle.java.home` และ `org.gradle.java.installations.paths` ให้ชี้ไปที่ JBR ของ Android Studio ใน [`android-app/gradle.properties`](../android-app/gradle.properties) พร้อมปิด auto-detect/auto-download
2. ลบไฟล์ `android-app/gradle/gradle-daemon-jvm.properties` ทิ้ง (ไฟล์นี้บังคับให้ Android Studio ใช้ฟีเจอร์ "Gradle Daemon JVM criteria" ซึ่ง auto-detect JDK 21 ในเครื่องแทนค่าที่ตั้งไว้ใน `gradle.properties`) — **ห้ามรันคำสั่ง `gradlew updateDaemonJvm` ซ้ำ** เพราะจะสร้างไฟล์นี้กลับมาและปัญหาจะเกิดซ้ำ
3. ใน Android Studio: **File → Sync Project with Gradle Files** แล้วตรวจว่า Settings → Build Tools → Gradle ไม่มี "Gradle JVM criteria" บังคับ auto-detect อีก
