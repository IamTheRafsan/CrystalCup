package com.example.foodapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation



class HomeScreen : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var seeAllTextView: TextView
    private lateinit var userName: TextView
    private lateinit var userLocation: TextView
    private lateinit var searchTextField : EditText
    private lateinit var searchButton: ImageView
    private lateinit var giftBox: LottieAnimationView
    private lateinit var userSharedPreferences: SharedPreferences
    private lateinit var locationSharedPreferences: SharedPreferences
    //----For the Menu
    val menuList: ArrayList<HashMap <String, String>> = ArrayList()
    var menuHashMap: HashMap<String, String>? = null
    private lateinit var adapter: HomeScreen.myAdapter
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuProgressBar: ProgressBar
    val cartList: ArrayList<HashMap <String, String>> = ArrayList()
    var food: String = " "



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home_screen, container, false)


        userNameTextView = view.findViewById(R.id.userName)
        locationTextView = view.findViewById(R.id.location)
        seeAllTextView = view.findViewById(R.id.seeAll)
        userName = view.findViewById(R.id.userName)
        userLocation = view.findViewById(R.id.location)
        giftBox = view.findViewById(R.id.giftBox)
        searchTextField = view.findViewById(R.id.searchFood)
        searchButton = view.findViewById(R.id.searchButton)
        menuProgressBar = view.findViewById(R.id.menuProgressBar)

        userSharedPreferences = requireActivity().getSharedPreferences("userPreferences", AppCompatActivity.MODE_PRIVATE)
        locationSharedPreferences = requireActivity().getSharedPreferences("deliveryPreferences", AppCompatActivity.MODE_PRIVATE)

        val uName = userSharedPreferences.getString("userName", "")
        val uLocation = locationSharedPreferences.getString("userAddress", "")

        if (userSharedPreferences.all.isEmpty()) {
            val intent = Intent(requireContext(), SignIn::class.java)
            startActivity(intent)
        } else {
            userName.text = "Hi, $uName"
            userLocation.text = "$uLocation"
        }


        //------Recycle View Setup
        menuProgressBar.visibility = View.VISIBLE
        menuRecyclerView = view.findViewById(R.id.menuRecyclerView)
        adapter = myAdapter()
        menuRecyclerView.adapter = adapter
        menuRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);

        //------Load the menu when the activity starts
        loadMenuFromWeb()

        searchButton.setOnClickListener {
            food = searchTextField.text.toString()
            loadMenuFromWeb()
        }

        giftBox.setOnClickListener(){
            val offersFragment = Offers()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, offersFragment)
                .addToBackStack(null) // Optional: Allows the user to navigate back to the previous fragment
                .commit()
        }

        return view;
    }


    //-------------Load Menu From the DataBase

    private fun loadMenuFromWeb() {

        menuList.clear()

        val url = "https://www.digitalrangersbd.com/app/ladidh/menu.php?f="+food


        val jsonArrayRequest = JsonArrayRequest(com.android.volley.Request.Method.GET, url, null, Response.Listener
        { response ->
            for (x in 0 until response.length()) {
                try {
                    val jsonObject = response.getJSONObject(x)
                    val itemName = jsonObject.getString("name")
                    val itemDescription = jsonObject.getString("description")
                    val itemPrice = jsonObject.getString("price")
                    val itemImage = jsonObject.getString("image")

                    menuHashMap = HashMap()
                    menuHashMap!!["itemName"] = itemName
                    menuHashMap!!["itemDescription"] = itemDescription
                    menuHashMap!!["itemPrice"] = itemPrice
                    menuHashMap!!["itemImage"] = itemImage
                    menuList.add(menuHashMap!!)

                    menuProgressBar.visibility = View.GONE

                } catch (e: Exception) {
                    // Handle general exceptions here
                    e.printStackTrace()
                }
            }
            if (menuList.size > 0) {
                adapter.notifyDataSetChanged()
            }
            if (menuList.isEmpty()) {
                adapter.notifyDataSetChanged()
                menuProgressBar.visibility = View.GONE

                AlertDialog.Builder(requireContext())
                    .setTitle("Sorry! No results found.")
                    .setMessage("Please search something else.")
                    .setNegativeButton("OK") { dialog, which -> }
                    .show()
            } else {
                adapter.notifyDataSetChanged()
                menuProgressBar.visibility = View.GONE
            }
        },
            Response.ErrorListener{ error ->
            }
        )

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonArrayRequest)
    }
//-------------Load Menu From the DataBase




    //--------------Adapter for RecycleView

    private inner class myAdapter : RecyclerView.Adapter<myAdapter.MenuViewHolder>() {

        inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var itemName : TextView = itemView.findViewById(R.id.itemName)
            var itemPrice : TextView = itemView.findViewById(R.id.itemPrice)
            var itemImage : ImageView = itemView.findViewById(R.id.itemImage)
            val addToCartButton : TextView = itemView.findViewById(R.id.addToCartButton)
            val goToCartButton : TextView = itemView.findViewById(R.id.goToCartButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.menu_item, parent, false)
            return MenuViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
            if (position < menuList.size) {

                val menuHashMap = menuList[position]
                val itemName = menuHashMap["itemName"]
                val itemDescription = menuHashMap["itemDescription"]
                val itemPrice = menuHashMap["itemPrice"]
                val itemImage = menuHashMap["itemImage"]

                holder.itemName.text = itemName
                holder.itemPrice.text = "TK. "+itemPrice
                Picasso.get().load(itemImage)
                    .fit()
                    .centerCrop()
                    .transform(RoundedCornersTransformation(30, 0))
                    .into(holder.itemImage)


                val sharedPreferences = holder.itemView.context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = sharedPreferences.getString("cartList", null)
                val type = object : TypeToken<ArrayList<HashMap<String, String>>>() {}.type
                var cartList = gson.fromJson<ArrayList<HashMap<String, String>>>(json, type)

                if (cartList == null) {
                    cartList = ArrayList()
                }

                val isInCart = cartList.any { it["itemName"] == itemName }

                if (isInCart) {
                    holder.addToCartButton.visibility = View.GONE
                    holder.goToCartButton.visibility = View.VISIBLE
                } else {
                    holder.addToCartButton.visibility = View.VISIBLE
                    holder.goToCartButton.visibility = View.GONE
                }

                holder.addToCartButton.setOnClickListener {
                    val cartHashMap = HashMap<String, String>()
                    cartHashMap["itemName"] = itemName ?: ""
                    cartHashMap["itemPrice"] = itemPrice ?: ""
                    cartHashMap["itemImage"] = itemImage ?: ""

                    // Ensure cartList is not null
                    if (cartList == null) {
                        cartList = ArrayList()
                    }

                    // Add the new item to the cartList
                    cartList?.add(cartHashMap)

                    // Update the adapter
                    notifyDataSetChanged()

                    // Update the shared preferences
                    val jsonCart = gson.toJson(cartList)
                    sharedPreferences.edit().putString("cartList", jsonCart).apply()

                    // Update the visibility of buttons
                    holder.addToCartButton.visibility = View.GONE
                    holder.goToCartButton.visibility = View.VISIBLE
                }



                holder.goToCartButton.setOnClickListener {
                    // Inside HomeScreen fragment where you want to navigate to Cart
                    val cartFragment = Cart()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, cartFragment)
                        .addToBackStack(null) // Optional: Allows the user to navigate back to the previous fragment
                        .commit()

                }
            }
        }

        override fun getItemCount(): Int {
            return menuList.size
        }
    }



}