package com.sef.sorrowtosmiles

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.sef.sorrowtosmiles.databinding.FragmentProfileBinding
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import org.json.JSONObject


/**
 * Profile Fragment
 */
class ProfileFragment : Fragment() {

    companion object{
        lateinit var instance: ProfileFragment
    }

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val login: Login by lazy { Login.instance }

    private lateinit var signInGoogleIntent: ActivityResultLauncher<Intent>
    private lateinit var signInTwitterButton: TwitterLoginButton

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instance = this

        refreshAccVisibleGone()
        setupButtons()

    }

    /**
     * Setups required buttons, show logged in accounts, hide log in buttons
     */
    private fun setupButtons(){

        binding.buttonLogout.setOnClickListener{
            MainActivity.instance.mGoogleSignInClient.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_LoginFragment)
        }


        signInGoogleIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            val data = result.data
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

        binding.connectGoogle.setOnClickListener{
            connectViaGoogle()
        }
        login.setOnSuccess{
            refreshAccVisibleGone()
        }
        login.setOnFail{
                e ->
            println(e)
            Toast.makeText(context, e, Toast.LENGTH_LONG).show()
        }
        login.setOnVolleyFail {
                e ->
            println(e)
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }


        signInTwitterButton = binding.connectTwitter
        signInTwitterButton.callback = object: Callback<TwitterSession>(){
            override fun success(result: com.twitter.sdk.android.core.Result<TwitterSession>) {
                println("Twitter Success")
                val data = result.data
                twitterTryConnect(data)
            }

            override fun failure(exception: TwitterException?) {
                println("Twitter Failure")
                println("Twitter Failed to get user data " + exception?.message)
                exception?.printStackTrace()
            }
        }

        val signInTelegramButton = binding.connectTelegram
        signInTelegramButton.setOnClickListener{
            login.connectTelegram(requireContext())
        }
    }

    /**
     * Refreshes an account to show if it's visible or gone
     */
    private fun refreshAccVisibleGone(){
        binding.textViewProfileName.text = login.loggedInUser?.name

        val user = login.loggedInUser!!
        println(binding.textViewTwitterStatus.text)
        setAccViews(arrayOf(binding.imageButtonTwitter, binding.textViewTwitterStatus, binding.connectTwitter), user.hasTwitter, user.twitterName)
        setAccViews(arrayOf(binding.imageButtonGoogle, binding.textViewGoogleStatus, binding.connectGoogle), user.hasGoogle, user.googleName)
        setAccViews(arrayOf(binding.imageButtonTelegram, binding.textViewTelegramStatus, binding.connectTelegram), user.hasTelegram, user.telegramUsername)

    }

    /**
     * Signs in Twitter
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        signInTwitterButton.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Connect Via Google
     */
    private fun connectViaGoogle(){
        val signInIntent = MainActivity.instance.mGoogleSignInClient.signInIntent
        signInGoogleIntent.launch(signInIntent)
    }

    /**
     * Handles Google Sign in results
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            googleTryConnect(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            println("FAILED TO CONNECT IN GOOGLE")
            println(e.statusCode)
            googleTryConnect(null)
        }
    }

    /**
     * Attempt to connect to google
     */
    private fun googleTryConnect(account: GoogleSignInAccount?) {
        if (account != null) {
            println("Connected - GOOGLE")
            println(account.displayName)
            println(account.email)
            println(account.id)

            val authCode = account.serverAuthCode
            if (authCode == null){
                login.connectGoogle(account.displayName!!, account.id!!, account.email!!, null)
            }
            else{
                println(authCode)
                val client_id = "90822878962-i00qcr18beaclqe22i2gttf4dmslcob5.apps.googleusercontent.com"
                val client_secret = "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"
                val json = JSONObject("{ 'grant_type': 'authorization_code', 'client_id': '$client_id', 'client_secret': '$client_secret', 'redirect_uri': '', 'code': '$authCode' }")

                URLRequest.jsonPOSTRequest(URLRequest.googleTokenRequest, json, {
                        result ->
                    println(result)
                    login.connectGoogle(account.displayName!!, account.id!!, account.email!!, result)
                },{

                })
            }

        }
    }


    /**
     * Attempts to connect to Twitter
     */
    private fun twitterTryConnect(account: TwitterSession){
        println("Connect - TWITTER")
        println(account.userId)
        println(account.userName)

        login.connectTwitter(account.userName, account.userId.toString())

    }


    /**
     * Sets the Account Views to show or hide
     */
    private fun setAccViews(views: Array<View>, exists: Boolean, connectedText: String){
        if (exists){
            setVisible(views[0])
            setVisible(views[1])
            setGone(views[2])
        }
        else{
            setGone(views[0])
            setGone(views[1])
            setVisible(views[2])
        }
        val textView = views[1] as TextView
        textView.text = connectedText
        println(textView.text)
    }

    /**
     * Hide a view
     */
    private fun setGone(view: View){
        view.visibility = View.GONE
    }

    /**
     * Show a view
     */
    private fun setVisible(view: View) {
        view.visibility = View.VISIBLE
    }

    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}