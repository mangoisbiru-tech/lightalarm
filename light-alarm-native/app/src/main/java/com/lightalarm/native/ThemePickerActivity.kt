package com.lightalarm.nativeapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ThemePickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_picker)

        val themeList: ListView = findViewById(R.id.themeList)
        val themes = listOf(
            "Sunrise", "Green Grass", "Blue Sea", "Sephora Blue",
            "Pastel Green", "Aurora", "Pink Ocean", "Lavender"
        )

        themeList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, themes)

        themeList.setOnItemClickListener { _, _, _, _ ->
            // Close for now; wiring selection can be added later
            finish()
        }
    }
}










