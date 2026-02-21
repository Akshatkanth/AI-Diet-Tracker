package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.DietTrackerActivity
import com.aidiettracker.R
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<MaterialButton>(R.id.button_view_plan).setOnClickListener {
            startActivity(Intent(this, DietPlanActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_track_diet).setOnClickListener {
            startActivity(Intent(this, DietTrackerActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
