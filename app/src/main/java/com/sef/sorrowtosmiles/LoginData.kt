package com.sef.sorrowtosmiles

import android.text.TextUtils
import android.util.Patterns
import org.json.JSONObject

/**
 * Contains login credentials
 */
class LoginData(var email: String, var password: String) {

    companion object{
        const val STRONG_PASSWORD_LENGTH : Int = 8

        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val COMMAND = "command"
        const val PACKET = "packet"
        const val COMMAND_LOGIN = "login"

        /**
         * Checks if a registering email is valid
         */
        fun isValidEmail(email: String): Boolean{
            if (TextUtils.isEmpty(email)){
                return false
            }
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        /**
         * Checks if a registering password is valid (>8 characters)
         */
        fun isValidPassword(password: String): Boolean{
            return password.length >= STRONG_PASSWORD_LENGTH
        }

    }


    /**
     * Converts the credentials into a json file for sending to the server
     */
    fun convertToJson(): JSONObject {
        return JSONObject("{ '$COMMAND': '$COMMAND_LOGIN', '$PACKET': { '$EMAIL': '$email', '$PASSWORD': '$password' }}")
    }

}
