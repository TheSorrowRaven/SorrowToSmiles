package com.sef.sorrowtosmiles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.android.volley.VolleyError
import org.json.JSONObject
import java.util.*

/**
 * Login Stuff
 */
class Login() {

    companion object{
        const val id = "id"
        const val accEmail = "accEmail"

        const val command = "command"
        const val packet = "packet"
        const val command_loginRegisterGoogle = "loginRegisterGoogle"
        const val command_loginRegisterTwitter = "loginRegisterTwitter"
        const val command_connectGoogle = "connectGoogle"
        const val command_connectTwitter = "connectTwitter"

        const val command_prepareTelegramID = "prepareTelegramID"

        val instance: Login = Login()
    }

    var loggedInUser: User? = null

    private lateinit var onFailCallback: (err: String) -> Unit
    private lateinit var onSuccessCallback: (user: User) -> Unit
    private lateinit var onVolleyFailCallback: (err: VolleyError) -> Unit


    /**
     * Sets the callback when network error occurs
     */
    fun setOnVolleyFail(onVolleyFail: (err: VolleyError) -> Unit){
        onVolleyFailCallback = onVolleyFail
    }

    /**
     * Sets the callback when logging in fails (Invalid credentials)
     */
    fun setOnFail(onFail: (err: String) -> Unit){
        onFailCallback = onFail
    }

    /**
     * Sets the callback when logging in succeeds
     */
    fun setOnSuccess(onSuccess: (user: User) -> Unit){
        onSuccessCallback = onSuccess
    }

    /**
     * Logins the logged in user, and navigate to the respective fragment
     */
    fun loginUser(navController: NavController){
        if (loggedInUser == null){
            println("ERROR - loggedInUser null, cannot login directly!")
            Toast.makeText(MainActivity.instance.applicationContext, "ERROR", Toast.LENGTH_LONG).show()
            return
        }
        val u: User = loggedInUser!!
        if (u.admin){
            navController.navigate(R.id.action_LoginFragment_to_adminMainMenuFragment)
            return
        }
        if (u.isPsychiatrist){
            navController.navigate(R.id.action_LoginFragment_to_psychiatristMainMenuFragment)
            return
        }
        navController.navigate(R.id.action_login_to_mainmenu)
    }

    /**
     * Attempts to login with credentials
     */
    fun tryLogin(loginData: LoginData){

        val loginDataJson = loginData.convertToJson()
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, loginDataJson,
        {
            response ->
                if (response.has("error")){
                    if (this::onFailCallback.isInitialized){
                        onFailCallback(response["error"].toString())
                    }
                    println(response["error"])
                }
                else{
                    val user = User.createUserFromJson(response)
                    verifyLogin(user)
                }
        },
        {
            err ->
                println("NETWORK ERROR - LOGIN")
                err.printStackTrace()
                if (this:: onVolleyFailCallback.isInitialized){
                    onVolleyFailCallback(err)
                }
        })

    }

    /**
     * Verifies a login if it fails or not
     */
    private fun verifyLogin(user: User?, callback: Boolean = true){
        if (user == null){
            println("Failed to create user pls fix")
            return
        }
        if (user.isPsychiatrist && !user.psychiatristVerified){
            Toast.makeText(MainActivity.instance.applicationContext, "This Psychiatrist Account has not yet been verified", Toast.LENGTH_LONG).show()
            return
        }
        loggedInUser = user
        println(loggedInUser)
        if (callback){
            if (this::onSuccessCallback.isInitialized){
                onSuccessCallback(user)
            }
        }
    }

    /**
     * Attempt to login via a json file containing a user
     */
    fun tryCreateUserLogin(json: JSONObject){
        val user = User.createUserFromJson(json)
        verifyLogin(user)
    }

    /**
     * Attempt to login via a json file containing a user (without calling callbacks) - normally for user refreshes
     */
    fun tryCreateUserLoginNoCallback(json: JSONObject){
        val user = User.createUserFromJson(json)
        verifyLogin(user, false)
    }


    /**
     * Login via google
     */
    fun loginViaGoogle(name: String, id: String, email: String, tokens: JSONObject?){
        var json: JSONObject
        if (tokens == null){
            val jsonStr = "{ '$command': '$command_loginRegisterGoogle', '$packet': {'name': '$name', 'id': '$id', 'email': '$email', '$accEmail': '$email'} }"
            json = JSONObject(jsonStr)
        }
        else{
            val tokensStr = tokens.toString()
            val jsonStr = "{ '$command': '$command_loginRegisterGoogle', '$packet': {'name': '$name', 'id': '$id', 'email': '$email', '$accEmail': '$email', 'tokens': $tokensStr} }"
            json = JSONObject(jsonStr)
        }
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            response ->
                tryCreateUserLogin(response)
        },{
            err ->
                err.printStackTrace()
        })
    }

    /**
     * Connects a google account
     */
    fun connectGoogle(name: String, id: String, email: String, tokens: JSONObject?){
        val myEmail = loggedInUser!!.email
        var json: JSONObject
        if (tokens == null){
            val jsonStr = "{ '$command': '$command_connectGoogle', '$packet': {'name': '$name', 'id': '$id', 'email': '$myEmail', '$accEmail': '$email'} }"
            json = JSONObject(jsonStr)
        }
        else{
            val tokensStr = tokens.toString()
            val jsonStr = "{ '$command': '$command_connectGoogle', '$packet': {'name': '$name', 'id': '$id', 'email': '$myEmail', '$accEmail': '$email', 'tokens': $tokensStr} }"
            json = JSONObject(jsonStr)
        }
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            response ->
                tryCreateUserLogin(response)
        },{
                err ->
            err.printStackTrace()
        })
    }

    /**
     * Logins via Twitter
     */
    fun loginViaTwitter(name: String, id: String){
        val jsonStr = "{ '$command': '$command_loginRegisterTwitter', '$packet': {'name': '$name', 'id': '$id', 'email': '$id'} }"
        val json = JSONObject(jsonStr)
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            response ->
                tryCreateUserLogin(response)
        },{
            err ->
                err.printStackTrace()
        })
    }

    /**
     * Connects a Twitter Account
     */
    fun connectTwitter(name: String, id: String){
        val baseEmail = loggedInUser!!.email
        val jsonStr = "{ '$command': '$command_connectTwitter', '$packet': {'name': '$name', 'id': '$id', 'email': '$baseEmail'} }"
        val json = JSONObject(jsonStr)
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            response ->
                tryCreateUserLogin(response)
        },{
                err ->
            err.printStackTrace()
        })
    }

    /**
     * Login via Telegram
     */
    fun loginViaTelegram(context: Context){

        val code = Base64.encode(UUID.randomUUID().toString().toByteArray(), Base64.DEFAULT).toString().replace('[', '0').replace('@', '-')

        URLRequest.jsonPOSTRequest(URLRequest.serverTelegramURL, JSONObject("{ 'command': '$command_prepareTelegramID', '$packet': { '$id': '$code' }}"),
        {
            response ->
                println("REQUESTING RESPONSE")
                println(response)

                val user = User.createUserFromJson(response)
                if (user != null){
                    Toast.makeText(context, "Telegram Session Log in", Toast.LENGTH_SHORT).show()
                    verifyLogin(user)
                }
                else{
                    val httpIntent = Intent(Intent.ACTION_VIEW)
                    val uri = URLRequest.telegramURL + code
                    println(uri)
                    httpIntent.data = Uri.parse(uri)
                    MainActivity.instance.startActivity(httpIntent)

                    Toast.makeText(context, "You have 1 minute to complete the Telegram authentication", Toast.LENGTH_SHORT).show()
                    URLRequest.jsonPOSTRequest(URLRequest.serverTelegramWaitURL, JSONObject("{ 'dud': 'dud' }"),
                        {
                            res ->
                                val u = User.createUserFromJson(res)
                                MainActivity.instance.onResumeCallback = {
                                    verifyLogin(u)
                                    MainActivity.instance.onResumeCallback = {}
                                }

                                Toast.makeText(context, "Logged In Via Telegram, Please return to the app", Toast.LENGTH_SHORT).show()

                        },{
                            err ->
                                println(err)
                                Toast.makeText(context, "Network Error - $err", Toast.LENGTH_LONG).show()
                        }, 60000)
                }



        },{
            err ->
                println(err)
                Toast.makeText(context, "Network Error, or fail to finish Telegram authentication within 1 minute", Toast.LENGTH_LONG).show()
        })
    }

    /**
     * Connect a Telegram account
     */
    fun connectTelegram(context: Context){

        val baseEmail = loggedInUser!!.email
        val code = Base64.encode(UUID.randomUUID().toString().toByteArray(), Base64.DEFAULT).toString().replace('[', '0').replace('@', '-')

        URLRequest.jsonPOSTRequest(URLRequest.serverTelegramURL, JSONObject("{ 'command': '$command_prepareTelegramID', '$packet': { '$id': '$code', 'email': '$baseEmail'}}"),
            {
                    response ->
                println("REQUESTING RESPONSE")
                println(response)

                val user = User.createUserFromJson(response)
                if (user != null){
                    Toast.makeText(context, "Telegram Session Log in", Toast.LENGTH_SHORT).show()
                    verifyLogin(user)
                }
                else{
                    val httpIntent = Intent(Intent.ACTION_VIEW)
                    val uri = URLRequest.telegramURL + code
                    println(uri)
                    httpIntent.data = Uri.parse(uri)
                    MainActivity.instance.startActivity(httpIntent)

                    Toast.makeText(context, "You have 1 minute to complete the Telegram authentication", Toast.LENGTH_SHORT).show()
                    URLRequest.jsonPOSTRequest(URLRequest.serverTelegramWaitURL, JSONObject("{ 'dud': 'dud' }"),
                        {
                                res ->
                            val u = User.createUserFromJson(res)
                            MainActivity.instance.onResumeCallback = {
                                verifyLogin(u)
                                MainActivity.instance.onResumeCallback = {}
                            }

                            Toast.makeText(context, "Logged In Via Telegram, Please return to the app", Toast.LENGTH_SHORT).show()

                        },{
                                err ->
                            println(err)
                            Toast.makeText(context, "Network Error - $err", Toast.LENGTH_LONG).show()
                        }, 60000)
                }



            },{
                    err ->
                println(err)
                Toast.makeText(context, "Network Error, or fail to finish Telegram authentication within 1 minute", Toast.LENGTH_LONG).show()
            })
    }


}