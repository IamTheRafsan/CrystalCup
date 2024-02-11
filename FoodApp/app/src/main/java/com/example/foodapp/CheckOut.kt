package com.example.foodapp

import UserDashboard
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.text.DecimalFormat

class CheckOut : AppCompatActivity() {

    private lateinit var totalPrice: TextView
    private lateinit var userName: TextView
    private lateinit var userMobile: TextView
    private lateinit var userLocation: TextView
    private lateinit var confirmButton: Button
    private lateinit var selectedItemsList: ArrayList<HashMap<String, String>>
    private var deliveryCharge: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_check_out)

        totalPrice = findViewById(R.id.total_price)
        userName = findViewById(R.id.userName)
        userMobile = findViewById(R.id.userMobile)
        userLocation = findViewById(R.id.userLocation)
        confirmButton = findViewById(R.id.confirmButton)


        val receivedIntent = intent
        if (receivedIntent != null) {
            selectedItemsList = receivedIntent.getSerializableExtra("selectedItems") as? ArrayList<HashMap<String, String>> ?: ArrayList()
            val couponPrice = receivedIntent.getDoubleExtra("couponPrice", 0.0)
            deliveryCharge = receivedIntent.getIntExtra("deliveryCharge", 0)
            var uLocation = receivedIntent.getStringExtra("userLocation", )

            val decimalFormat = DecimalFormat("#.##")
            val formattedCouponPrice = decimalFormat.format(couponPrice)
            totalPrice.text = formattedCouponPrice
            userLocation.text = uLocation
        }

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("userPreferences", MODE_PRIVATE)

        val uName = sharedPreferences.getString("userName", "")
        val uMobile = sharedPreferences.getString("userMobile", "")


        userName.text = uName
        userMobile.text = uMobile



        confirmButton.setOnClickListener(){


            if (sharedPreferences.all.isEmpty()) {

                AlertDialog.Builder(this)
                    .setTitle("Not Logged In!")
                    .setMessage("Please login to order.")
                    .setNegativeButton("OK") { dialog, which ->
                        val intent = Intent(this, SignIn::class.java)
                        startActivity(intent)
                    }
                    .show()

            } else {

                val CustomerName = userName.text.toString()
                val CustomerMobile = userMobile.text.toString()
                val CustomerLocation = userLocation.text.toString()
                val CustomerBill = totalPrice.text.toString()
                val CustomerDeliveryCharge = deliveryCharge.toString()
                val Items = formatSelectedItems(selectedItemsList)
                val status = "pending"

                val url = "https://www.digitalrangersbd.com/app/ladidh/order.php?n="+CustomerName+"&m="+CustomerMobile+"&l="+CustomerLocation+"&i="+Items+"&b="+CustomerBill+"&d="+CustomerDeliveryCharge+"&s="+status

                if(CustomerName.isEmpty() || CustomerMobile.isEmpty() || CustomerLocation.isEmpty())
                {
                    AlertDialog.Builder(this)
                        .setTitle("Empty Fields!")
                        .setMessage("Please put all the information.")
                        .setNegativeButton("OK") { dialog, which -> }
                        .show()

                }
                else
                {
                    val stringRequest = StringRequest(
                        Request.Method.GET, url, Response.Listener<String> { response ->
                        },
                        Response.ErrorListener { error ->
                        })

                    val requestQueue = Volley.newRequestQueue(this)
                    requestQueue.add(stringRequest)

                    AlertDialog.Builder(this)
                        .setTitle("Congrats! Order Confirmed.")
                        .setMessage("Note: Delivery charge applied separately.")
                        .setNegativeButton("OK") { dialog, which ->
                            val intent = Intent(this, OrderTrack::class.java)
                            startActivity(intent)
                        }
                        .show()
                    userName.text = " "
                    userMobile.text = " "
                    userLocation.text = " "

                }


            }



        }
    }
    private fun formatSelectedItems(selectedItemsList: ArrayList<HashMap<String, String>>): String {
        val itemsStringBuilder = StringBuilder()

        for (item in selectedItemsList) {
            val itemName = item["itemName"] ?: ""
            val itemCount = item["itemCount"] ?: "0"

            itemsStringBuilder.append("$itemName: $itemCount, ")
        }

        return itemsStringBuilder.toString().trimEnd(',', ' ')
    }



}
