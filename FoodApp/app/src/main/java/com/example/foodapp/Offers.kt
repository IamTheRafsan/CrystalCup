package com.example.foodapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso


class Offers : Fragment() {

    private lateinit var offerRecyleView: RecyclerView
    private lateinit var adapter: Offers.myAdapter
    private var offerList: ArrayList<HashMap<String, String>> = ArrayList()
    private var offerHashMap: HashMap<String, String>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_offers, container, false)

        offerRecyleView = view.findViewById(R.id.offerRecycleView)

        adapter = myAdapter()
        offerRecyleView.adapter = adapter
        offerRecyleView.layoutManager = LinearLayoutManager(requireContext())

        loadOffers()

        return view
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
    fun loadOffers() {
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
        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonArrayRequest)
    }


}