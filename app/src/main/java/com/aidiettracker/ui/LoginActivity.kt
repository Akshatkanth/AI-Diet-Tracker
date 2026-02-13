package com.aidiettracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aidiettracker.R
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<MaterialButton>(R.id.button_login).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.button_register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
