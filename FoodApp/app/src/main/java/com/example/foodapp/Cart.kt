package com.example.foodapp


import android.Manifest
import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.io.IOException
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class Cart : AppCompatActivity() {

    private lateinit var itemTotalTextView: TextView
    private lateinit var totalPriceTextView: TextView
    private lateinit var deliveryChargeTextView: TextView
    private lateinit var couponApplyButton: Button
    private lateinit var checkOutButton: Button
    private lateinit var couponCodeTextField: EditText
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        couponCodeTextField = findViewById(R.id.couponCode)
        itemTotalTextView = findViewById(R.id.itemTotal)
        totalPriceTextView = findViewById(R.id.totalPrice)
        deliveryChargeTextView = findViewById(R.id.deliveryCharge)
        checkOutButton = findViewById(R.id.checkOutButton)
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        couponApplyButton = findViewById(R.id.couponApplyButton)


// Cart recycler View adapter call
        val adapter = myAdapter()
        cartRecyclerView.adapter = adapter
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        // Update the RecyclerView with the retrieved data
        val receivedIntent = intent
        if (receivedIntent != null) {
            val receivedMenuList = receivedIntent.getSerializableExtra("cartList") as? ArrayList<HashMap<String, String>>

            if (receivedMenuList != null) {
                this.cartList.addAll(receivedMenuList)
            }
        }


        //----location

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!checkLocationPermission()) {
            requestLocationPermission()
        } else {
            requestLocation()
        }

        //----location end

        couponApplyButton.setOnClickListener() {

            couponCodeCheck()

        }
        checkOutButton.setOnClickListener() {

            goToCheckOut()

        }
    }


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
                        AlertDialog.Builder(this)
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

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonArrayRequest)
    }




    //---------------Check logged IN and Go to checkOut

    private fun goToCheckOut() {


        if (cartList.isEmpty()) {

            AlertDialog.Builder(this)
                .setTitle("Not Logged In!")
                .setMessage("Please login to order.")
                .setNegativeButton("OK") { dialog, which ->
                    val intent = Intent(this, SignIn::class.java)
                    startActivity(intent)
                }
                .show()

        } else {

            if (deliveryCharge == 0) {

                AlertDialog.Builder(this)
                    .setTitle("Location not found!")
                    .setMessage("Please give Location Permission and turn on the Device Location.")
                    .setNegativeButton("OK") { dialog, which ->
                    }
                    .show()

            } else {

                if (couponPrice != 0.0) {
                    val intent = Intent(this, CheckOut::class.java)

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
                    AlertDialog.Builder(this)
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
                    if (holder.itemCount > 0) {
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



    //-----------location permission

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestLocation() {
        try {
            val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000) // Update every 10 seconds

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation = locationResult.lastLocation
                    handleLocationSuccess(lastLocation)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            handleLocationFailure(e)
        }
    }


    //-------------Calculate the delivery charge
    private fun handleLocationSuccess(location: Location?) {
        location?.let {
            val userLatitude = it.latitude
            val userLongitude = it.longitude
            // Use latitude and longitude as needed

            val shopLatitude = 22.35293952799264
            val shopLongitude = 91.82533493598135

            val distance = calculateDistance(shopLatitude, shopLongitude, userLatitude, userLongitude)
            userAddress = getAddressFromLocation(userLatitude, userLongitude)




            if(distance <= 3){

                deliveryCharge = 60

            }
            else if(distance>3 && distance<=4){

                deliveryCharge = 80

            }
            else if(distance>4 && distance<=5){

                deliveryCharge = 100

            }
            else if(distance>5 && distance<=6){

                deliveryCharge = 120

            }
            else if(distance>6 && distance<=8){

                deliveryCharge = 150

            }
            else if(distance>8 && distance<= 10){

                deliveryCharge = 200

            }
            else if(distance > 10){

                deliveryCharge = 99

            }

            deliveryChargeTextView.text= deliveryCharge.toString()
            updatePrice()




        } ?: run {
            // Handle the case where location is null
            handleLocationFailure(Exception("Location is null"))
        }
    }


    private fun handleLocationFailure(exception: Exception) {
        // Handle location retrieval failure, such as no location available
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, request location
                    requestLocation()
                } else {
                    // Permission denied, handle accordingly
                }
            }
        }
    }


    //--------Calculate distance for delivery

    fun calculateDistance(
        shopLatitude: Double,
        shopLongitude: Double,
        userLatitude: Double,
        userLongitude: Double
    ): Double {
        val R = 6371.0 // Earth radius in kilometers

        val dLat = Math.toRadians(userLatitude - shopLatitude)
        val dLon = Math.toRadians(userLongitude - shopLongitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(shopLatitude)) * cos(Math.toRadians(userLatitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Distance in kilometers
    }

    fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                return address.getAddressLine(0) ?: ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

}
