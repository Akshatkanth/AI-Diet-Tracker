package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<MaterialButton>(R.id.button_save_profile).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}
