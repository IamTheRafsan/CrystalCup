package com.example.foodapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso

class Offers : AppCompatActivity() {

    private lateinit var offerRecyleView: RecyclerView
    private lateinit var adapter: myAdapter
    private var offerList: ArrayList<HashMap<String, String>> = ArrayList()
    private var offerHashMap: HashMap<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offers)

        offerRecyleView = findViewById(R.id.offerRecycleView)

        adapter = myAdapter()
        offerRecyleView.adapter = adapter
        offerRecyleView.layoutManager = LinearLayoutManager(this)

        loadData()

    }

    //==================Recycle View Adapter
    private inner class myAdapter : RecyclerView.Adapter<myAdapter.OfferViewHolder>() {

        inner class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var offerImage: ImageView = itemView.findViewById(R.id.offerImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.offer_layout, parent, false)
            return OfferViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
            if (position < offerList.size) {
                var offerHashMap = offerList[position]
                val image = offerHashMap["image"]

                Picasso.get().load(image).into(holder.offerImage)

            }
        }

        override fun getItemCount(): Int {
            return offerList.size
        }
    }

    //================Load Offer Data
    fun loadData() {
        val url = "https://www.digitalrangersbd.com/app/ladidh/offer.php"

        val jsonArrayRequest = JsonArrayRequest(url, Response.Listener { response ->
            for (x in 0 until response.length()) {
                try {
                    val jsonObject = response.getJSONObject(x)

                    val image = jsonObject.getString("image").replace("//", "/")

                    offerHashMap = HashMap()
                    offerHashMap!!["image"] = image
                    offerList.add(offerHashMap!!)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (offerList.size > 0) {
                adapter.notifyDataSetChanged()

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
