package com.sef.sorrowtosmiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.VolleyError
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.sef.sorrowtosmiles.databinding.FragmentLoginBinding
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import io.ktor.util.*
import org.json.JSONObject
import org.telegram.passport.PassportScope
import org.telegram.passport.PassportScopeElementOne
import org.telegram.passport.PassportScopeElementOneOfSeveral
import org.telegram.passport.TelegramPassport
import java.util.*
import com.twitter.sdk.android.core.models.Tweet





/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 *
 * Login Fragment
 */
class LoginFragment : Fragment() {

    companion object{
        lateinit var instance: LoginFragment
    }

    private var _binding: FragmentLoginBinding? = null

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
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        instance = this

        super.onViewCreated(view, savedInstanceState)
        MainActivity.instance.hideBackAndSetBackAsExit()


        initializeLogin()

        setup()
    }

    private fun setup(){

        binding.buttonLoginGotoRegister.setOnClickListener{
            exitToFragment(R.id.action_login_to_register)
        }

        /**
         * Google Login Intent for later use
         */
        signInGoogleIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            val data = result.data
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)

        }


        val emailET = binding.loginEmail
        val passwordET = binding.loginPassword
        binding.buttonLogin.setOnClickListener {

            val email: String = emailET.text.toString()
            val password: String = passwordET.text.toString()
            tryLogin(email, password)
        }

        binding.imageButtonGoogle.setOnClickListener{
            signInViaGoogle()
        }
        signInTwitterButton = binding.imageButtonTwitter
        /**
         * Twitter Login Flow
         */
        signInTwitterButton.callback = object: Callback<TwitterSession>(){
            override fun success(result: com.twitter.sdk.android.core.Result<TwitterSession>) {
                println("Twitter Success")
                val data = result.data
                twitterTrySignIn(data)
            }

            override fun failure(exception: TwitterException?) {
                println("Twitter Failure")
                println("Twitter Failed to get user data " + exception?.message)
                exception?.printStackTrace()
            }
        }

        val signInTelegramButton = binding.imageButtonTelegram
        signInTelegramButton.setOnClickListener{
            login.loginViaTelegram(requireContext())
        }
    }

    /**
     * Only Twitter uses onActivityResult
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        signInTwitterButton.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * On Start, try to auto sign in Google
     */
    override fun onStart(){
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        googleTrySignIn(account)
    }

    /**
     * Attempt to sign in via Google
     */
    private fun signInViaGoogle(){
        val signInIntent = MainActivity.instance.mGoogleSignInClient.signInIntent
        signInGoogleIntent.launch(signInIntent)

    }

    /**
     * Handles the sign in request of Google
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            googleTrySignIn(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            println("FAILED TO SIGN IN GOOGLE")
            println(e.statusCode)
            googleTrySignIn(null)
        }
    }

    /**
     * After account is gotten, sign in by sending to the server
     */
    private fun googleTrySignIn(account: GoogleSignInAccount?) {
        if (account != null) {
            println("Signed In - GOOGLE")
            println(account.displayName)
            println(account.email)
            println(account.id)

            val authCode = account.serverAuthCode
            if (authCode == null){
                login.loginViaGoogle(account.displayName!!, account.id!!, account.email!!, null)
            }
            else{
                println(authCode)
                val client_id = "90822878962-i00qcr18beaclqe22i2gttf4dmslcob5.apps.googleusercontent.com"
                val client_secret = "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"
                val json = JSONObject("{ 'grant_type': 'authorization_code', 'client_id': '$client_id', 'client_secret': '$client_secret', 'redirect_uri': '', 'code': '$authCode' }")

                URLRequest.jsonPOSTRequest(URLRequest.googleTokenRequest, json, {
                        result ->
                    println(result)
                    login.loginViaGoogle(account.displayName!!, account.id!!, account.email!!, result)
                },{

                })
            }



        }
    }

    /**
     * Sign in via Twitter
     */
    private fun twitterTrySignIn(account: TwitterSession){
        println("Signed In - TWITTER")
        println(account.userId)
        println(account.userName)

        login.loginViaTwitter(account.userName, account.userId.toString())

    }

    /**
     * Sets callbacks on Login
     */
    private fun initializeLogin(){
        login.setOnVolleyFail(this::notifyNetworkError)
        login.setOnFail(this::notifyLoginFailed)
        login.setOnSuccess(this::successfulLogin)
    }

    /**
     * Try to login (normal login) with email and password
     */
    private fun tryLogin(email: String, password: String){
        val loginData = LoginData(email, password)
        login.tryLogin(loginData)
    }

    /**
     * Notifies the user of any network error when signing in
     */
    private fun notifyNetworkError(error: VolleyError){
        Toast.makeText(requireContext(), "Network Error - " + error.message, Toast.LENGTH_LONG).show()
    }

    /**
     * Notifies a login has failed (invalid credentials)
     */
    private fun notifyLoginFailed(err: String){
        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
    }

    /**
     * Logs in a user on success
     */
    private fun successfulLogin(user: User){
        login.loginUser(instance.findNavController())
        println("Logging in navigation")
        println(login.loggedInUser)
    }

    /**
     * Exit to registration Fragment
     */
    private fun exitToFragment(id: Int, reset: Boolean = true){
        findNavController().navigate(id)
        if (reset){
            MainActivity.instance.showRestoreBack()
        }
    }

    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}