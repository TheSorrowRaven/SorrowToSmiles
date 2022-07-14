package com.sef.sorrowtosmiles

import org.json.JSONObject
import java.time.LocalDate

/**
 * Registration
 */
class Register (val view: RegisterFragment) {

    companion object{
        const val COMMAND = "command"
        const val PACKET = "packet"
        const val COMMAND_REGISTER = "register"
        const val ERROR = "error"
    }

    var username = ""

    /**
     * Registers a user/psychiatrist
     */
    fun registerUser(user: User){

        val jsonUser = user.convertToJson()

        val json = JSONObject("{ '$COMMAND': '$COMMAND_REGISTER', '$PACKET': $jsonUser }")

        println(json)

        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json,
        {
            response ->
                if (response.has(ERROR)){
                    view.registerError(response[ERROR].toString())
                }
                else{
                    val user = User.createUserFromJson(response, true)
                    if (user != null){
                        username = user.username
                        view.registerSuccessful(user)
                    }
                }

        },
        {
            error ->
                println("REGISTER ERROR")
                error.printStackTrace()
                view.registerVolleyError(error)
        })
    }



}