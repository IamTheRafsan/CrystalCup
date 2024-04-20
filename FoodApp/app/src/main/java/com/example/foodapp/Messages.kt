package com.example.foodapp

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.print.PrintAttributes.Margins
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject


class Messages : Fragment() {

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var message: EditText
    private lateinit var sendButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBar2: ProgressBar
    private lateinit var adapter: Messages.MyAdapter
    val messageList: ArrayList<HashMap<String, String>> = ArrayList()
    var messageHashMap: HashMap<String, String>? = null
    private lateinit var sharedPreferences: SharedPreferences

    val adminMobile = "admin"
    val adminName = "admin"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        messageRecyclerView = view.findViewById(R.id.messageRecyclerView)
        message = view.findViewById(R.id.message)
        sendButton = view.findViewById(R.id.sendButton)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar2 = view.findViewById(R.id.progressBar2)

        progressBar.visibility = View.VISIBLE

        adapter = MyAdapter()
        messageRecyclerView.adapter = adapter
        messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        loadMessages()


        sendButton.setOnClickListener {

            sendMessage()

        }


        return view
    }


    //-----------Load All Messages

    fun loadMessages(){

        sharedPreferences = requireActivity().getSharedPreferences(
            "userPreferences",
            AppCompatActivity.MODE_PRIVATE
        )
        val userMobile = sharedPreferences.getString("userMobile", "")

        messageList.clear()
        val messageJsonArrayRequest = JsonArrayRequest(Request.Method.GET,
            "https://www.digitalrangersbd.com/app/ladidh/chatLoad.php?s=$userMobile&r=$adminMobile",
            null,
            Response.Listener { response ->
                for (x in 0 until response.length()) {
                    try {
                        val jsonObject: JSONObject = response.getJSONObject(x)
                        val sender = jsonObject.getString("sender")
                        val message = jsonObject.getString("message")

                        val messageHashMap = HashMap<String, String>()
                        messageHashMap["sender"] = sender
                        messageHashMap["message"] = message
                        messageList.add(messageHashMap)

                        progressBar.visibility = View.GONE

                    } catch (e: JSONException) {
                        throw RuntimeException(e)
                    }
                }
                adapter.notifyDataSetChanged()
                messageRecyclerView.scrollToPosition(messageList.size - 1)
            },
            Response.ErrorListener {
                // Handle error
            })

        val requestQueue: RequestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(messageJsonArrayRequest)
    }



    //---------Send Messages

    fun sendMessage(){

        sharedPreferences = requireActivity().getSharedPreferences("userPreferences", AppCompatActivity.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")
        val userMobile = sharedPreferences.getString("userMobile", "")


        val url =
            "https://www.digitalrangersbd.com/app/ladidh/chatSend.php?sn=$userName&rn=$adminName&s=$userMobile&r=$adminMobile&m=${message.text.toString()}"

        if (message.length() > 0) {
            progressBar2.visibility = View.VISIBLE
            val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    progressBar2.visibility = View.GONE
                    message.setText("")

                    loadMessages()
                    progressBar.visibility = View.VISIBLE

                },
                Response.ErrorListener {
                    // Handle error
                })

            val requestQueue: RequestQueue = Volley.newRequestQueue(requireContext())
            requestQueue.add(stringRequest)
        } else {
            progressBar2.visibility = View.GONE

            AlertDialog.Builder(requireContext())
                .setTitle("No messages typed")
                .setMessage("Please type a message")
                .setNegativeButton("OK") { dialog, which -> }
                .show()
        }
    }

//-------------Messages Adapter for recycle view
private inner class MyAdapter : RecyclerView.Adapter<Messages.MyAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var texts: TextView = itemView.findViewById(R.id.texts)
        var messageBox: LinearLayout = itemView.findViewById(R.id.messageBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val myView: View = inflater.inflate(R.layout.chat, parent, false)
        return MyViewHolder(myView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position < messageList.size) {
            if (position < messageList.size) {
                val messageMap: HashMap<String, String> = messageList[position]
                val sender: String? = messageMap["sender"]
                val message: String? = messageMap["message"]

                // Set different background colors based on the sender
                if (sender == adminMobile) {
                    // If the sender is the admin, set background color for admin messages
                    holder.messageBox.setBackgroundResource(R.drawable.button_background)
                    holder.texts.gravity = Gravity.START
                    holder.texts.setTextColor(Color.WHITE)
                } else {
                    // If the sender is the user, set background color for user messages
                    holder.messageBox.setBackgroundResource(R.drawable.box_background)
                    holder.texts.gravity = Gravity.END
                    holder.texts.setTextColor(Color.BLACK)
                }

                // Set the message text
                holder.texts.text = message
            }
        }
    }
        override fun getItemCount(): Int {
            return messageList.size
        }
    }


}