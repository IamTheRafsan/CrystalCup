import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.foodapp.R
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.HomeScreen
import com.example.foodapp.MainActivity
import com.example.foodapp.OrderHistory
import com.example.foodapp.OrderTrack
import com.example.foodapp.SignIn
import com.example.foodapp.UpdateProfile


class UserDashboard : Fragment() {

    private lateinit var userName: TextView
    private lateinit var userMobile: TextView
    private lateinit var userLocation: TextView
    private lateinit var logoutButton: LinearLayout
    private lateinit var orderHistoryButton: LinearLayout
    private lateinit var orderTrackButton: LinearLayout
    private lateinit var backButton: ImageView
    private lateinit var editButton: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_dashboard, container, false)

        userName = view.findViewById(R.id.userName)
        userMobile = view.findViewById(R.id.userMobile)
        userLocation = view.findViewById(R.id.userLocation)
        logoutButton = view.findViewById(R.id.logoutButton)
        orderHistoryButton = view.findViewById(R.id.orderHistoryButton)
        orderTrackButton = view.findViewById(R.id.orderTrackButton)
        backButton = view.findViewById(R.id.backButton)
        editButton = view.findViewById(R.id.editButton)

        sharedPreferences = requireActivity().getSharedPreferences("userPreferences", AppCompatActivity.MODE_PRIVATE)

        val uName = sharedPreferences.getString("userName", "")
        val uMobile = sharedPreferences.getString("userMobile", "")
        val uLocation = sharedPreferences.getString("userLocation", "")

        if (sharedPreferences.all.isEmpty()) {
            val intent = Intent(requireContext(), SignIn::class.java)
            startActivity(intent)
        } else {
            userName.text = uName
            userLocation.text = uLocation
            userMobile.text = uMobile
        }

        logoutButton.setOnClickListener(){
            AlertDialog.Builder(requireContext())
                .setTitle("Logout?")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("No") { dialog, which ->
                }
                .setPositiveButton("Yes"){ dialog, which ->
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                    // Redirect to the SignIn activity after logout
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                }
                .show()
        }

        orderTrackButton.setOnClickListener(){
            val intent = Intent(requireContext(), OrderTrack::class.java)
            startActivity(intent)
        }

        orderHistoryButton.setOnClickListener(){
            val intent = Intent(requireContext(), OrderHistory::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener(){
            val intent = Intent(requireContext(), HomeScreen::class.java)
            startActivity(intent)
        }

        editButton.setOnClickListener(){
            val intent = Intent(requireContext(), UpdateProfile::class.java)
            startActivity(intent)
        }

        return view
    }
}
