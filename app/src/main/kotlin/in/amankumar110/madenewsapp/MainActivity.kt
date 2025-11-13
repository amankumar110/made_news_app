package `in`.amankumar110.madenewsapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import `in`.amankumar110.madenewsapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import `in`.amankumar110.madenewsapp.viewmodel.auth.EmailVerificationViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var keepScreen = true
    private val emailVerificationViewModel: EmailVerificationViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepScreen }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Register callback to know when NavHostFragment's view is created
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                fragment: Fragment,
                view: View,
                savedInstanceState: Bundle?
            ) {
                if (fragment is NavHostFragment && fragment.id == R.id.nav_host_fragment) {
                    navController = fragment.navController
                    observeEmailFlows()
                    emailVerificationViewModel.verifyEmail()
                    supportFragmentManager.unregisterFragmentLifecycleCallbacks(this)
                }
            }
        }, false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity)
        }

        window.navigationBarColor = getColor(R.color.app_color_background)
        window.statusBarColor = getColor(R.color.app_color_background)
    }


    private fun observeEmailFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                emailVerificationViewModel.emailVerificationEvent.collect { event ->
                    when (event) {
                        EmailVerificationViewModel.EmailVerificationEvent.NotVerified -> {
                            Log.v("Test1", "Email not verified")
                            navigateTo(R.id.signInFragment)
                        }
                        EmailVerificationViewModel.EmailVerificationEvent.Verified -> {
                            Log.v("Test1", "Email verified")
                            keepScreen = false
                        }
                        EmailVerificationViewModel.EmailVerificationEvent.UserNotLoggedIn -> {
                            Log.v("Test1", "User not logged in")
                            navigateTo(R.id.signInFragment)
                        }
                        is EmailVerificationViewModel.EmailVerificationEvent.Error -> {
                            Log.e("Test1", "Error: ${event.message}")
                            navigateTo(R.id.signInFragment)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun navigateTo(destinationId: Int) {
        if (keepScreen) {
            navController.navigate(destinationId)  // use cached NavController
            keepScreen = false
        }
    }

}
