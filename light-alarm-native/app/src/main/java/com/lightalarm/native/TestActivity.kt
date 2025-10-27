package com.lightalarm.nativeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        val textView = TextView(this).apply {
            text = "Light Alarm Native - Test Mode\n\n" +
                   "✅ App is running!\n" +
                   "✅ No crashes detected\n" +
                   "✅ Ready for main app"
            textSize = 18f
            setPadding(0, 0, 0, 32)
        }
        
        val launchButton = Button(this).apply {
            text = "Launch Main App"
            setOnClickListener {
                val intent = Intent(this@TestActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
        
        layout.addView(textView)
        layout.addView(launchButton)
        setContentView(layout)
    }
}
