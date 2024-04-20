package com.example.foodapp

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class OrderTrack : AppCompatActivity() {
    private lateinit var orderTrackRecycleView: RecyclerView
    private lateinit var adapter: MyAdapter
    val ordersList: ArrayList<HashMap<String, String>> = ArrayList()
    var ordersHashMap: HashMap<String, String>? = null
    private lateinit var progressBar : ProgressBar
    private lateinit var backButton: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_track)

        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        ordersList.clear()

        orderTrackRecycleView = findViewById(R.id.orderTrackRecycleView)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE


        adapter = MyAdapter()
        orderTrackRecycleView.adapter = adapter
        orderTrackRecycleView.layoutManager = LinearLayoutManager(this)

        loadOrders()

    }

    //=======recycle view Adapter
    private inner class MyAdapter : RecyclerView.Adapter<MyAdapter.MenuViewHolder>() {

        inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var name: TextView = itemView.findViewById(R.id.name)
            var location: TextView = itemView.findViewById(R.id.location)
            var items: TextView = itemView.findViewById(R.id.items)
            var bill: TextView = itemView.findViewById(R.id.bill)
            var cancelButton: TextView = itemView.findViewById(R.id.cancelOrderButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.order_track_layout, parent, false)
            return MenuViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {

            if (position < ordersList.size) {

                val ordersHashMap = ordersList[position]
                val id = ordersHashMap["id"]
                val name = ordersHashMap["name"]
                val mobile = ordersHashMap["mobile"]
                val location = ordersHashMap["location"]
                val items = ordersHashMap["items"]
                val bill = ordersHashMap["bill"]
                val status = ordersHashMap["status"]
                val date = ordersHashMap["date"]

                holder.name.text = name
                holder.location.text = location
                holder.items.text = items
                holder.bill.text = "Tk. $bill"

                if(status=="pending"){
                    holder.cancelButton.visibility = View.VISIBLE
                }
                else{
                    holder.cancelButton.visibility = View.GONE
                }

                holder.cancelButton.setOnClickListener() {
                    val url = "https://www.digitalrangersbd.com/app/ladidh/cancelOrder.php?i="+id

                    val stringRequest = StringRequest(
                        Request.Method.GET, url,
                        Response.Listener<String> { response ->
                        },
                        Response.ErrorListener { error ->
                        })

                    val requestQueue = Volley.newRequestQueue(holder.itemView.context)
                    requestQueue.add(stringRequest)

                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Cancelled!")
                        .setMessage("Order has been cancelled.")
                        .setNegativeButton("OK") { _, _ -> }
                        .show()

                    loadOrders()
                }

            }
        }

        override fun getItemCount(): Int {
            return ordersList.size
        }
    }

    //=======load orders
    private fun loadOrders() {
        ordersList.clear()

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("userPreferences", MODE_PRIVATE)
        val uMobile = sharedPreferences.getString("userMobile", "")

        val url = "https://digitalrangersbd.com/app/ladidh/loadOrders.php?m=$uMobile"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                for (x in 0 until response.length()) {
                    try {
                        val jsonObject = response.getJSONObject(x)
                        val id = jsonObject.getString("id")
                        val name = jsonObject.getString("name")
                        val mobile = jsonObject.getString("mobile")
                        val location = jsonObject.getString("location")
                        val items = jsonObject.getString("items")
                        val bill = jsonObject.getString("bill")
                        val status = jsonObject.getString("status")
                        val date = jsonObject.getString("date")

                        ordersHashMap = HashMap()
                        ordersHashMap!!["id"] = id
                        ordersHashMap!!["name"] = name
                        ordersHashMap!!["mobile"] = mobile
                        ordersHashMap!!["location"] = location
                        ordersHashMap!!["items"] = items
                        ordersHashMap!!["bill"] = bill
                        ordersHashMap!!["status"] = status
                        ordersHashMap!!["date"] = date

                        ordersList.add(ordersHashMap!!)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                progressBar.visibility = View.GONE
                if (ordersList.isNotEmpty()) {
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.notifyDataSetChanged()
                    AlertDialog.Builder(this)
                        .setTitle("No Orders Found")
                        .setMessage("You haven't placed any orders yet.")
                        .setNegativeButton("OK") { dialog, which ->
                            finish()
                        }
                        .show()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }
        )

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }
}