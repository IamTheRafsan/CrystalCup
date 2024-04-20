package com.example.foodapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley


class UpdateProfile : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var userMobile: EditText
    private lateinit var userLocation: EditText
    private lateinit var userPassword: EditText
    private lateinit var userConfirmPassword: EditText
    private lateinit var updateButton: Button
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        userName = findViewById(R.id.name)
        userMobile = findViewById(R.id.mobile)
        userLocation = findViewById(R.id.location)
        userPassword = findViewById(R.id.password)
        userConfirmPassword = findViewById(R.id.confirmPassword)
        updateButton = findViewById(R.id.updateButton)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        updateButton.setOnClickListener(){

            val Uname = userName.text.toString()
            val Umobile = userMobile.text.toString()
            val Ulocation = userLocation.text.toString()
            val Upassword = userPassword.text.toString()
            val Uconfirm = userConfirmPassword.text.toString()

            if (Uname.isNotEmpty() && Umobile.isNotEmpty() && Upassword.isNotEmpty() && Ulocation.isNotEmpty() && Uconfirm.isNotEmpty()){
                if (Upassword == Uconfirm){


                    val url = "https://www.digitalrangersbd.com/app/ladidh/updateUser.php?n="+Uname+"&m="+Umobile+"&l="+Ulocation+"&p="+Upassword

                    val stringRequest = StringRequest(com.android.volley.Request.Method.GET, url,
                        Response.Listener<String> { response ->
                        },
                        Response.ErrorListener { error ->
                        })

                    val requestQueue = Volley.newRequestQueue(this)
                    requestQueue.add(stringRequest)

                    AlertDialog.Builder(this)
                        .setTitle("Profile Updated Successfully!")
                        .setMessage("Information update was successful.")
                        .setNegativeButton("OK") { _, _ ->
                            userName.text.clear()
                            userMobile.text.clear()
                            userLocation.text.clear()
                            userPassword.text.clear()
                            userConfirmPassword.text.clear()

                            val sharedPreferences: SharedPreferences = this.getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
                            val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()
                            val intent = Intent(this, SignIn::class.java)
                            startActivity(intent)

                        }
                        .show()



                }
                else
                {
                    AlertDialog.Builder(this)
                        .setTitle("Password Did Not Match!")
                        .setMessage("Please confirm with the same password.")
                        .setNegativeButton("OK") { _, _ -> }
                        .show()
                }

            }
            else
            {
                AlertDialog.Builder(this)
                    .setTitle("Empty Field!")
                    .setMessage("Please fill in all the fields.")
                    .setNegativeButton("OK") { _, _ -> }
                    .show()
            }
        }
    }
}
