package com.felixtek.kmitlmappilot

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- ส่วนที่เราเพิ่มเข้ามาสำหรับเชื่อมต่อ Backend ---
        val btnLoadMap = findViewById<Button>(R.id.btnLoadMap)
        val tvData = findViewById<TextView>(R.id.tvData)
        val btnOpenActivity2 = findViewById<Button>(R.id.btnOpenActivity2)

        btnOpenActivity2.setOnClickListener {
            startActivity(Intent(this, Activity2::class.java))
        }

        btnLoadMap.setOnClickListener {
            tvData.text = "กำลังดึงข้อมูล... กรุณารอสักครู่"

            // สร้าง Thread แยก (เพราะ Android ห้ามโหลดข้อมูลผ่านเน็ตในจังหวะเดียวกับการวาดหน้าจอหลัก)
            thread {
                try {
                    // ใช้ 10.0.2.2 เพื่อชี้ไปหา localhost ของคอมพิวเตอร์ที่รัน Docker อยู่
                    //val apiUrl = URL("http://10.0.2.2:3000/api/locations") // ของเดิม (รันในเครื่อง)
                    val apiUrl = URL("http://34.87.153.162:3000/api/locations") // ของใหม่ (ยิงหาคลาวด์)

                    val response = apiUrl.readText()

                    // เมื่อได้ข้อมูลแล้ว สลับกลับมาอัปเดตข้อความบนหน้าจอหลัก
                    runOnUiThread {
                        tvData.text = response
                    }
                } catch (e: Exception) {
                    // ถ้าเกิดข้อผิดพลาด เช่น ลืมเปิด Docker หรือหาเซิร์ฟเวอร์ไม่เจอ
                    runOnUiThread {
                        tvData.text = "เกิดข้อผิดพลาด!\n\n${e.message}"
                    }
                }
            }
        }

        // --- ส่วนทดสอบระบบแจ้งปัญหา (Issue Report) ---
        val categoryLabels = arrayOf("ของหาย", "สิ่งอำนวยความสะดวกชำรุด", "อื่นๆ")
        val categoryCodes = arrayOf("lost_item", "facility", "other")

        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoryLabels
        )

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSendReport = findViewById<Button>(R.id.btnSendReport)

        btnSendReport.setOnClickListener {
            val description = etDescription.text.toString().trim()
            if (description.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกรายละเอียดปัญหา", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val category = categoryCodes[spinnerCategory.selectedItemPosition]

            tvData.text = "กำลังส่งแจ้งปัญหา... กรุณารอสักครู่"

            thread {
                try {
                    //val apiUrl = URL("http://10.0.2.2:3000/api/reports") //localhost
                    val apiUrl = URL("http://34.87.153.162:3000/api/reports") //cloud
                    val connection = apiUrl.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    val body = JSONObject().apply {
                        put("description", description)
                        put("category", category)
                    }
                    connection.outputStream.use { it.write(body.toString().toByteArray()) }

                    val responseCode = connection.responseCode
                    val response = if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        connection.errorStream?.bufferedReader()?.use { it.readText() }
                            ?: "HTTP $responseCode"
                    }

                    runOnUiThread {
                        tvData.text = response
                        if (responseCode in 200..299) {
                            etDescription.text.clear()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        tvData.text = "เกิดข้อผิดพลาด!\n\n${e.message}"
                    }
                }
            }
        }
    }
}