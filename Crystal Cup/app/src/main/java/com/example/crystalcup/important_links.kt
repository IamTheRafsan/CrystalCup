package com.example.crystalcup

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class important_links : AppCompatActivity() {

    private lateinit var privacyPolicyButton: TextView
    private lateinit var deleteAccountButton: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_important_links)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        privacyPolicyButton = findViewById(R.id.privacyPolicy)
        deleteAccountButton = findViewById(R.id.deleteAccount)
        backButton = findViewById(R.id.backButton)

    }
}