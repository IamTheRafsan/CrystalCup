package com.example.foodapp

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UserDashboard : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var userMobile: TextView
    private lateinit var userLocation: TextView
    private lateinit var logoutButton: LinearLayout
    private lateinit var orderHistoryButton: LinearLayout
    private lateinit var orderTrackButton: LinearLayout
    private lateinit var backButton: ImageView
    private lateinit var editButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_dashboard)

        userName = findViewById(R.id.userName)
        userMobile = findViewById(R.id.userMobile)
        userLocation = findViewById(R.id.userLocation)
        logoutButton = findViewById(R.id.logoutButton)
        orderHistoryButton = findViewById(R.id.orderHistoryButton)
        orderTrackButton = findViewById(R.id.orderTrackButton)
        backButton = findViewById(R.id.backButton)
        editButton = findViewById(R.id.editButton)


        val sharedPreferences: SharedPreferences = this.getSharedPreferences("userPreferences", MODE_PRIVATE)

        val uName = sharedPreferences.getString("userName", "")
        val uMobile = sharedPreferences.getString("userMobile", "")
        val uLocation = sharedPreferences.getString("userLocation", "")

        if (sharedPreferences.all.isEmpty()) {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        } else {
        }

        userName.text = uName
        userLocation.text = uLocation
        userMobile.text = uMobile

        logoutButton.setOnClickListener(){
            AlertDialog.Builder(this)
                .setTitle("Logout?")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("No") { dialog, which ->

                }
                .setPositiveButton("Yes"){ dialog, which ->
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                }
                .show()
        }

        orderTrackButton.setOnClickListener(){
            val intent = Intent(this, OrderTrack::class.java)
            startActivity(intent)
        }

        orderHistoryButton.setOnClickListener(){
            val intent = Intent(this, OrderHistory::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener(){
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }

        editButton.setOnClickListener(){
            val intent = Intent(this, UpdateProfile::class.java)
            startActivity(intent)
        }
    }



}
