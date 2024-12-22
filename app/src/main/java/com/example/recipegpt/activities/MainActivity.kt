import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.recipegpt.R
import com.example.recipegpt.databinding.ActivityMainBinding
import com.example.recipegpt.fragments.HomeFragment
import com.example.recipegpt.fragments.SavedRecipesFragment
import com.example.recipegpt.fragments.ShoppingListFragment
import com.example.recipegpt.receivers.NetworkChangeReceiver
import com.example.recipegpt.services.RecipeFetchService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register NetworkChangeReceiver
        networkChangeReceiver = NetworkChangeReceiver()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        // Handle Bottom Navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.menu_saved_recipes -> {
                    replaceFragment(SavedRecipesFragment())
                    true
                }
                R.id.menu_shopping_list -> {
                    replaceFragment(ShoppingListFragment())
                    true
                }
                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Start RecipeFetchService
        startRecipeFetchService()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    private fun startRecipeFetchService() {
        val intent = Intent(this, RecipeFetchService::class.java)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver) // Unregister the receiver
    }
}
