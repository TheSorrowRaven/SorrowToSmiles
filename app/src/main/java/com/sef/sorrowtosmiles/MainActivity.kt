package com.sef.sorrowtosmiles

//Github hello
//Android Studio hello

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewbinding.ViewBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.sef.sorrowtosmiles.databinding.ActivityMainBinding
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import kotlin.system.exitProcess
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope


/**
 * Main Activity of the app
 */
class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MainActivity
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    lateinit var mGoogleSignInClient: GoogleSignInClient

    /**
     * Default Override
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        instance = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        initialization()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


    }

    /**
     * Twitter calls the activity intent instead of the fragment's intent, thus pass it to Login
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LoginFragment.instance.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Default Override
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Initialize the application
     */
    private fun initialization(){
        Login()
        initializeTwitterGoogle()

        URLRequest.initialize(applicationContext)

    }

    /**
     * Initialize Twitter and Google values
     */
    private fun initializeTwitterGoogle(){
        /**
         * Twitter has to build at this point before initialization
         */
        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(
                TwitterAuthConfig(
                    resources.getString(R.string.twitter_api_key),
                    resources.getString(R.string.twitter_api_secret))
            )
            .debug(true)
            .build()
        Twitter.initialize(config)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            //.requestServerAuthCode("90822878962-i00qcr18beaclqe22i2gttf4dmslcob5.apps.googleusercontent.com")
            //.requestScopes(Scope("https://www.googleapis.com/auth/calendar"))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    var onResumeCallback: () -> Unit = {}


    /**
     * Calls Resuming fragment callback when frozen (mainly used in Telegram login flow)
     */
    override fun onResumeFragments() {
        super.onResumeFragments()
        println("RESUMING FRAGMENT")
        onResumeCallback()
    }


    private var backAsExit = false

    /**
     * Quits the app
     */
    private fun quitApp(){
        finish()
        exitProcess(0)
    }

    /**
     * Prevents the user to using back as logout, and exit instead
     */
    override fun onBackPressed() {
        if (backAsExit){
            quitApp()
        }
        else{
            super.onBackPressed()
        }
    }

    /**
     * Restores the back button to default behaviour
     */
    fun showRestoreBack(){
        hideBackButton(false)
        backAsExit = false
    }

    /**
     * Set back button to exit the program
     */
    fun hideBackAndSetBackAsExit(){
        hideBackButton(true)
        backAsExit = true
    }


    /**
     * Suppress option bar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (backAsExit){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Hides or reveals the action bar buttons
     */
    private fun hideBackButton(hide: Boolean){
        supportActionBar?.setDisplayHomeAsUpEnabled(!hide)
        supportActionBar?.setHomeButtonEnabled(!hide)
        actionBar?.setDisplayHomeAsUpEnabled(!hide)
        actionBar?.setHomeButtonEnabled(!hide)
    }

    /**
     * Default Override
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
