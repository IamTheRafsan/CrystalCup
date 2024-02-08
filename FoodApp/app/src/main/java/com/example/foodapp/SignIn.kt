package com.example.foodapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException


class SignIn : AppCompatActivity() {

    private lateinit var userMobile: EditText
    private lateinit var userPassword: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        userMobile = findViewById(R.id.mobile)
        userPassword = findViewById(R.id.password)
        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)


       //-----Back Button
        backButton.setOnClickListener {
            val myIntent = Intent(this, HomeScreen::class.java)
            startActivity(myIntent)
        }

        //-----Go to Sign Up
        signUpButton.setOnClickListener {
            val myIntent = Intent(this, SignUp::class.java)
            startActivity(myIntent)
        }

        //-------Sign In User
        signInButton.setOnClickListener(View.OnClickListener {

            val userMobile: String = userMobile.text.toString()
            val userPassword: String = userPassword.text.toString()

            if (userMobile.isNotEmpty() && userPassword.isNotEmpty()) {

                progressBar.setVisibility(View.VISIBLE)

                val url3 = "https://www.digitalrangersbd.com/app/ladidh/userLogin.php?m="+userMobile+"&p="+userPassword

                val jsonArrayRequest = JsonArrayRequest(
                    Request.Method.GET, url3, null,
                    { response ->
                        val result = response.toString()

                        if (result.length < 3) {
                            progressBar.setVisibility(View.GONE)
                            AlertDialog.Builder(this)
                                .setTitle("Wrong Mobile or Password!")
                                .setMessage("Please put all information correctly.")
                                .setNegativeButton("OK") { dialog, which -> }
                                .show()
                        } else {
                            val url4 = "https://www.digitalrangersbd.com/app/ladidh/userDetail.php?m="+userMobile+"&p="+userPassword
                            val detailArrayRequest = JsonArrayRequest(
                                Request.Method.GET, url4, null,
                                { response ->
                                    for (x in 0 until response.length()) {
                                        try {
                                            val jsonObject = response.getJSONObject(x)
                                            val name = jsonObject.getString("name")
                                            val mobile = jsonObject.getString("mobile")
                                            val location = jsonObject.getString("location")

                                            val sharedPreferences: SharedPreferences = getSharedPreferences("userPreferences", MODE_PRIVATE)
                                            val editor = sharedPreferences.edit()
                                            editor.putString("userMobile", mobile)
                                            editor.putString("userLocation", location)
                                            editor.putString("userName", name)
                                            editor.apply()

                                            val myintent = Intent(this, UserDashboard::class.java)
                                            startActivity(myintent)

                                            progressBar.visibility = View.GONE

                                        } catch (e: JSONException) {
                                            throw RuntimeException(e)
                                        }
                                    }
                                },
                                { error ->
                                    progressBar.visibility = View.GONE
                                    AlertDialog.Builder(this)
                                        .setTitle("Error")
                                        .setMessage("Error in userDetail.php request: ${error.message}")
                                        .setNegativeButton("OK") { _, _ -> }
                                        .show()
                                }
                            )

                            val requestQueue = Volley.newRequestQueue(applicationContext)
                            requestQueue.add(detailArrayRequest)


                        }
                    },
                    { error ->
                        progressBar.setVisibility(View.GONE)
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Error in userLogin.php request: ${error.message}")
                            .setNegativeButton("OK") { _, _ -> }
                            .show()
                    }
                )

                val requestQueue = Volley.newRequestQueue(applicationContext)
                requestQueue.add(jsonArrayRequest)
            } else {
                progressBar.setVisibility(View.GONE)
                AlertDialog.Builder(this)
                    .setTitle("Empty Field!")
                    .setMessage("Please fill in all the fields.")
                    .setNegativeButton("OK") { dialog, which -> }
                    .show()
            }
        })

    }
    override fun onBackPressed() {
        val homeIntent = Intent(this, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
        super.onBackPressed()
    }
}
