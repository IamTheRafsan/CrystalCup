package com.example.foodapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException


class SignUp : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var userMobile: EditText
    private lateinit var userLocation: EditText
    private lateinit var userPassword: EditText
    private lateinit var userConfirmPassword: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userName = findViewById(R.id.name)
        userMobile = findViewById(R.id.mobile)
        userLocation = findViewById(R.id.location)
        userPassword = findViewById(R.id.password)
        userConfirmPassword = findViewById(R.id.confirmPassword)
        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)


        //-----Back Button
        backButton.setOnClickListener {
            finish()
        }
        //--------Go to sign in page
        signInButton.setOnClickListener {
            val myIntent = Intent(this, SignIn::class.java)
            startActivity(myIntent)

        }

        //-------Register User
        signUpButton.setOnClickListener {

            val name = userName.text.toString()
            val mobile = userMobile.text.toString()
            val location = userLocation.text.toString()
            val password = userPassword.text.toString()
            val confirm = userConfirmPassword.text.toString()
            val url =
                "https://www.digitalrangersbd.com/app/ladidh/user.php?n=" + name + "&m=" + mobile + "&l=" + location + "&p=" + password
            val url2 = "https://www.digitalrangersbd.com/app/ladidh/regcon.php?m=" + mobile

            if (name.isNotEmpty() && mobile.isNotEmpty() && password.isNotEmpty()) {
                if (password == confirm) {

                    progressBar.visibility = View.VISIBLE

                    val jsonArrayRequest = JsonArrayRequest(
                        Request.Method.GET, url2,
                        null,
                        Response.Listener<JSONArray> { response ->
                            val result = response.toString()

                            if (result.length > 3) {
                                progressBar.visibility = View.GONE
                                AlertDialog.Builder(this)
                                    .setTitle("Mobile Already Exists!")
                                    .setMessage("Please use a new mobile number.")
                                    .setNegativeButton("OK") { _, _ -> }
                                    .show()
                            } else {
                                val stringRequest = StringRequest(
                                    Request.Method.GET, url,
                                    Response.Listener<String> { _ -> },
                                    Response.ErrorListener { _ -> }
                                )

                                val requestQueue =
                                    Volley.newRequestQueue(this.applicationContext)
                                requestQueue.add(stringRequest)
                                progressBar.visibility = View.GONE

                                AlertDialog.Builder(this)
                                    .setTitle("Congratulations!")
                                    .setMessage("Your Registration Is Completed!")
                                    .setNegativeButton("OK") { _, _ -> }
                                    .show()
                            }
                        },
                        Response.ErrorListener { _ -> }
                    )

                    val requestQueue =
                        Volley.newRequestQueue(this.applicationContext)
                    requestQueue.add(jsonArrayRequest)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Password Did Not Match!")
                        .setMessage("Please confirm with the same password.")
                        .setNegativeButton("OK") { _, _ -> }
                        .show()
                }
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Empty Field!")
                    .setMessage("Please fill in all the fields.")
                    .setNegativeButton("OK") { _, _ -> }
                    .show()
            }
        }


    }



    override fun onBackPressed() {

        finish()
        super.onBackPressed()
    }



}
