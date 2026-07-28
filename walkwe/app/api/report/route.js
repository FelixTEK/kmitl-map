// app/api/report/route.js — endpoint จริงคือ /api/report (ต้องตรงกับ fetch("/api/report", ...) ฝั่ง ReportPage.jsx)

import sql from "../../../lib/db.js";

export async function GET() {
  try {
    const reports = await sql`
      SELECT * FROM issue_reports ORDER BY report_date DESC
    `;

    return Response.json({
      success: true,
      reports,
    });
  } catch (err) {
    return Response.json(
      { success: false, error: err.message },
      { status: 500 }
    );
  }
}

export async function POST(req) {
  try {
    const body = await req.json();

    // 🔍 validate ฝั่ง server ด้วย (กันกรณี client bypass การเช็คในฟอร์ม หรือมีคนยิง request ตรงๆ)
    const subject = (body.subject || "").trim();
    const detail = (body.detail || "").trim();
    const faculty = body.faculty || "";
    const building = body.building || "";
    const floor = body.floor || "";
    const room = (body.room || "").trim();

    if (!subject || !detail) {
      return Response.json({ success: false, error: "กรุณากรอกหัวเรื่องและรายละเอียด" }, { status: 400 });
    }
    if (!faculty || !building || !floor || !room) {
      return Response.json({ success: false, error: "กรุณาระบุสถานที่ให้ครบ: คณะ อาคาร ชั้น และห้อง" }, { status: 400 });
    }

    // 🗂️ รับโครงสร้างแบนตามที่ ReportPage.jsx ส่งจริง ({name, subject, type, date, detail, faculty, building, floor, room})
    const reportCode = "RPT-" + Date.now();
    const reporterName = body.name || "";
    const type = body.type || "ทั่วไป";
    const reportDate = body.date ? new Date(body.date) : new Date();

    const [row] = await sql`
      INSERT INTO issue_reports
        (report_code, reporter_name, subject, type, detail, faculty, building, floor, room, report_date)
      VALUES
        (${reportCode}, ${reporterName}, ${subject}, ${type}, ${detail}, ${faculty}, ${building}, ${floor}, ${room}, ${reportDate})
      RETURNING *
    `;

    const report = {
      id: row.report_code,
      requester: row.reporter_name,
      subject: row.subject,
      category: row.type,
      date: row.report_date,
      detail: row.detail,
      location: { faculty: row.faculty, building: row.building, floor: row.floor, room: row.room },
      status: row.status,
      createdAt: row.created_at,
    };

    return Response.json({
      success: true,
      message: "Report submitted successfully.",
      report,
    });
  } catch (err) {
    return Response.json(
      { success: false, error: err.message },
      { status: 500 }
    );
  }
}
