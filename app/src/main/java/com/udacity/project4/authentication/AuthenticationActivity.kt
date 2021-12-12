package com.udacity.project4.authentication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.REQUEST_CODE_LOCATION
import kotlinx.android.synthetic.main.activity_authentication.*


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel: LoginViewModel = LoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        binding.spinner.visibility = View.GONE

        binding.loginButton.visibility = View.GONE

        binding.loginButton.setOnClickListener {
            launchSignInFlow()
        }

        observeAuthenticateState()

    }


    private fun launchSignInFlow() {
        binding.loginButton.isEnabled = false
        binding.spinner.visibility = View.VISIBLE
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                binding.loginButton.isEnabled = true
            }
        }
    }

    private fun observeAuthenticateState() {
        viewModel.authenticationState.observe(this) {
            when (it) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.d(TAG, "User is Authenticated")
                    spinner.visibility = View.GONE
                    startActivity(Intent(this, RemindersActivity::class.java))
                    finish()
                } else -> {
                    binding.loginButton.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = true
                    binding.loginButton.setOnClickListener { launchSignInFlow() }
                    Log.d(TAG, "User is not Authenticated")
                }
            }
        }
    }


//   TODO: a bonus is to customize the sign in flow to look nice using : -Maybe
    //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

}
