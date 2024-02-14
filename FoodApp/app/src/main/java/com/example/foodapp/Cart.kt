package com.example.foodapp


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.io.IOException
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Cart : Fragment() {

    private lateinit var itemTotalTextView: TextView
    private lateinit var totalPriceTextView: TextView
    private lateinit var deliveryChargeTextView: TextView
    private lateinit var couponApplyButton: Button
    private lateinit var checkOutButton: Button
    private lateinit var deleteCartButton: ImageView
    private lateinit var couponCodeTextField: EditText
    private lateinit var sharedPreferences: SharedPreferences
    //For the cart
    var cartList: ArrayList<HashMap<String, String>> = ArrayList()
    private lateinit var cartRecyclerView: RecyclerView
    //For the coupon
    var couponList: ArrayList<HashMap<String, String>> = ArrayList()
    var couponHashMap: HashMap<String, String>? = null
    var couponPrice: Double = 0.00
    //Calculations
    var itemTotal: Double = 0.00
    var deliveryCharge : Int = 0
    //Locations
    private lateinit var locationManager: LocationManager
    private lateinit var userAddress: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    //Required for getting the location
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        couponCodeTextField = view.findViewById(R.id.couponCode)
        itemTotalTextView = view.findViewById(R.id.itemTotal)
        totalPriceTextView = view.findViewById(R.id.totalPrice)
        deliveryChargeTextView = view.findViewById(R.id.deliveryCharge)
        checkOutButton = view.findViewById(R.id.checkOutButton)
        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        deleteCartButton = view.findViewById(R.id.deleteCartButton)
        couponApplyButton = view.findViewById(R.id.couponApplyButton)


// Cart recycler View adapter call
        val adapter = myAdapter()
        cartRecyclerView.adapter = adapter
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        //------Get the Cart List and Menu from the SharedPreference
        getCartListFromSharedPreferences()

        //------Get the Location and delivery charges from the SharedPreference from Splash Screen
        sharedPreferences = requireActivity().getSharedPreferences("deliveryPreferences", AppCompatActivity.MODE_PRIVATE)
        deliveryCharge = sharedPreferences.getString("deliveryCharge", "0")?.toIntOrNull() ?: 0
        userAddress = sharedPreferences.getString("userAddress", "") ?: ""
        deliveryChargeTextView.text = deliveryCharge.toString()

        couponApplyButton.setOnClickListener() {

            couponCodeCheck()

        }
        checkOutButton.setOnClickListener() {

            goToCheckOut()

        }

        deleteCartButton.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Clear Cart")
                .setMessage("Are you sure you want to clear your cart?")
                .setPositiveButton("Yes") { dialog, which ->
                    val sharedPreferences = requireContext().getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()

                    val homeFragment = HomeScreen()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, homeFragment)
                        .addToBackStack(null) // Optional: Allows the user to navigate back to the previous fragment
                        .commit()
                }
                .setNegativeButton("No") { dialog, which -> }
                .show()
        }

        return view
    }


    //------Get the Cart List and Menu from the SharedPreference
    private fun getCartListFromSharedPreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("CartPreferences", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("cartList", null)
        val type = object : TypeToken<ArrayList<HashMap<String, String>>>() {}.type
        cartList =  gson.fromJson(json, type) ?: ArrayList()

        if (cartList != null && cartList.isNotEmpty()) {
            // Convert the cartList to a readable string
            val cartListString = StringBuilder()
            for (item in cartList) {
                val itemName = item["itemName"] ?: ""
                val itemCount = item["itemCount"] ?: ""
                cartListString.append("$itemName: $itemCount\n")
            }

        }
    }
    //------Get the Cart List and Menu from the SharedPreference




    // --------- Coupon Code Check and Apply
    private fun couponCodeCheck() {
        val couponCode = couponCodeTextField.text.toString()
        couponList.clear()

        val url = "https://www.digitalrangersbd.com/app/ladidh/coupon.php?c="+couponCode

        val jsonArrayRequest = JsonArrayRequest(com.android.volley.Request.Method.GET, url, null, Response.Listener
        { response ->

            for (x in 0 until response.length()) {
                try {
                    val jsonObject = response.getJSONObject(x)
                    val code = jsonObject.getString("code")
                    val percent = jsonObject.getString("percent")
                    val max = jsonObject.getString("max")
                    val state = jsonObject.getString("state")

                    couponHashMap = HashMap()
                    couponHashMap!!["code"] = code
                    couponHashMap!!["percent"] = percent
                    couponHashMap!!["max"] = max
                    couponHashMap!!["state"] = state

                    couponList.add(couponHashMap!!)

                    var discount:Double = 0.00

                    if ( couponList.isEmpty() )
                    {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Sorry! Invalid Coupon")
                            .setMessage("Please put a valid coupon")
                            .setNegativeButton("OK") { dialog, which -> }
                            .show()

                    }else{
                        discount = percent.toDouble() * couponPrice / 100

                        if (discount <= max.toDouble()) {
                            couponPrice = (couponPrice - discount)+deliveryCharge.toDouble()
                            val decimalFormat = DecimalFormat("#.##") // Adjust the pattern based on your requirements
                            val formattedItemTotal = decimalFormat.format(couponPrice)
                            totalPriceTextView.text = formattedItemTotal
                            couponList.clear()
                            couponApplyButton.visibility = View.GONE
                        } else {
                            couponPrice = couponPrice - max.toDouble()
                            val decimalFormat = DecimalFormat("#.##") // Adjust the pattern based on your requirements
                            val formattedItemTotal = decimalFormat.format(couponPrice)
                            totalPriceTextView.text = formattedItemTotal
                            couponApplyButton.visibility = View.GONE
                            couponList.clear()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }
        )

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonArrayRequest)
    }




    //---------------Check logged IN and Go to checkOut

    private fun goToCheckOut() {


        if (cartList.isEmpty()) {

            AlertDialog.Builder(requireContext())
                .setTitle("Not Logged In!")
                .setMessage("Please login to order.")
                .setNegativeButton("OK") { dialog, which ->
                    val intent = Intent(requireContext(), SignIn::class.java)
                    startActivity(intent)
                }
                .show()

        } else {

            if (deliveryCharge == 0) {

                AlertDialog.Builder(requireContext())
                    .setTitle("Location not found!")
                    .setMessage("Please give Location Permission and turn on the Device Location.")
                    .setNegativeButton("OK") { dialog, which ->
                    }
                    .show()

            } else {

                if (couponPrice != 0.0) {
                    val intent = Intent(requireContext(), CheckOut::class.java)

                    val selectedItemsList = ArrayList<HashMap<String, String>>()

                    for (i in 0 until cartList.size) {
                        val menuHashMap = cartList[i]
                        val itemName = menuHashMap["itemName"] ?: ""
                        val itemCount = menuHashMap["itemCount"] ?: "0"

                        val selectedItem = HashMap<String, String>()
                        selectedItem["itemName"] = itemName
                        selectedItem["itemCount"] = itemCount

                        selectedItemsList.add(selectedItem)
                    }

                    var totalPrice = couponPrice + deliveryCharge

                    intent.putExtra("selectedItems", selectedItemsList)
                    intent.putExtra("couponPrice", totalPrice)
                    intent.putExtra("deliveryCharge", deliveryCharge)
                    intent.putExtra("userLocation", userAddress)

                    startActivity(intent)

                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Empty Cart")
                        .setMessage("Please select an item")
                        .setNegativeButton("OK") { dialog, which -> }
                        .show()
                }


            }


        }


    }

    //------------Price Update
    fun updatePrice() {

        var itemTotal: Double = 0.00
        var grandTotal: Double = 0.00

        for (i in 0 until cartList.size) {
            val menuHashMap = cartList[i]
            val price = menuHashMap["itemPrice"] ?: "0"
            val itemCount = menuHashMap["itemCount"] ?: "0"
            val itemSubtotal: Double = price.toDouble() * itemCount.toDouble()

            // Accumulate item total without delivery charge
            itemTotal += itemSubtotal

            // Accumulate grand total with delivery charge
            grandTotal += itemSubtotal
        }

        // Add delivery charge to the grand total
        grandTotal += deliveryCharge.toDouble()

        val decimalFormat = DecimalFormat("#.##") // Adjust the pattern based on your requirements
        val formattedItemTotal = decimalFormat.format(itemTotal)
        val formattedGrandTotal = decimalFormat.format(grandTotal)

        itemTotalTextView.text = formattedItemTotal
        totalPriceTextView.text = formattedGrandTotal
        couponPrice = itemTotal // Use grandTotal for couponPrice, as it includes delivery charge
    }



    //-------------Cart Recycler View adapter

    private inner class myAdapter : RecyclerView.Adapter<myAdapter.CartViewHolder>() {

        inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var itemName: TextView = itemView.findViewById(R.id.itemName)
            var itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
            var itemImage: ImageView = itemView.findViewById(R.id.itemImage)
            var itemAmount: TextView = itemView.findViewById(R.id.itemAmount)
            var plus: ImageView = itemView.findViewById(R.id.plus)
            var minus: ImageView = itemView.findViewById(R.id.minus)
            var itemCount = 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.cart_item, parent, false)
            return CartViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
            if (position < cartList.size) {
                var menuHashMap = cartList[position]
                val itemName = menuHashMap["itemName"]
                val itemDescription = menuHashMap["itemDescription"]
                val itemPrice = menuHashMap["itemPrice"]
                val itemImage = menuHashMap["itemImage"]

                holder.itemName.text = itemName
                holder.itemPrice.text = "TK. $itemPrice"
                Picasso.get().load(itemImage)
                    .fit()
                    .centerCrop()
                    .transform(RoundedCornersTransformation(30, 0))
                    .into(holder.itemImage)

                holder.itemCount++
                holder.itemAmount.text = holder.itemCount.toString()

                menuHashMap["itemCount"] = holder.itemCount.toString()

                updatePrice()

                holder.itemAmount.text = holder.itemCount.toString()
                menuHashMap["itemCount"] = holder.itemCount.toString()

                holder.plus.setOnClickListener {
                    holder.itemCount++
                    holder.itemAmount.text = holder.itemCount.toString()

                    menuHashMap["itemCount"] = holder.itemCount.toString()

                    updatePrice()
                }

                holder.minus.setOnClickListener {
                    if (holder.itemCount > 1) {
                        holder.itemCount--
                        holder.itemAmount.text = holder.itemCount.toString()

                        menuHashMap["itemCount"] = holder.itemCount.toString()

                        updatePrice()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return cartList.size
        }
    }


}