package com.sef.sorrowtosmiles

import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * The User Class, containing all sorts of information about a user
 */
class User(val name: String, val email: String, val username: String, val password: String, val phoneNo: String, val dob: LocalDate, val gender: Char, val isPsychiatrist: Boolean) {

    companion object{

        const val COMMAND = "command"
        const val PACKET = "packet"
        const val COMMAND_REGISTER = "register"
        const val ERROR = "error"
        const val USERNAME = "username"
        const val NAME = "name"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val PHONENO = "phoneno"
        const val GENDER = "gender"    //Char?
        const val DOB = "dob"  //Date
        const val ADMIN = "admin"   //True/False
        const val PSYCHIATRIST = "psychiatrist"    //True/False
        const val PSYCHIATRISTVERIFIED = "psychiatristVerified" //True/False

        const val FRIENDS = "friends"

        const val PINGS = "pings"
        const val PENDINGPINGS = "pendingPings"
        const val PASTPINGS = "pastPings"
        const val IAMFINE = "iamfine"
        const val DATETIME = "datetime"

        const val helpPsychiatrist = "helpPsychiatrist"
        const val patients = "patients"
        const val appointments = "appointments"
        const val MEETLINK = "meetLink"


        /**
         * Creates a user from a json containing this user's data
         */
        fun createUserFromJson(json: JSONObject, registration: Boolean = false) : User?{
            println(json)
            try{
                val name = json[NAME].toString()
                val email = json[EMAIL].toString()
                val username = json[USERNAME].toString()
                val password = json[PASSWORD].toString()
                val phoneNo = json[PHONENO].toString()
                val dobStr = json[DOB].toString()
                var dob = LocalDate.now()
                try{
                    dob = LocalDate.parse(dobStr)
                }
                catch(e: Exception){

                }
                val gender = json[GENDER].toString().single()
                var psychiatrist = false
                var psychiatristVerified = true
                if (json.has(PSYCHIATRIST)){
                    psychiatrist = json[PSYCHIATRIST] == "true"
                    if (json.has(PSYCHIATRISTVERIFIED)){
                        psychiatristVerified = json[PSYCHIATRISTVERIFIED] as Boolean
                        if (!registration && !psychiatristVerified){
                            Toast.makeText(MainActivity.instance.applicationContext, "Psychiatrist account is not yet verified! Lazy admins!", Toast.LENGTH_LONG).show()
                            return null
                        }
                    }
                }
                var admin = false
                if (json.has(ADMIN)){
                    admin = json[ADMIN] == "true"
                }
                val u = User(name, email, username, password, phoneNo, dob, gender, psychiatrist)
                u.psychiatristVerified = psychiatristVerified
                u.admin = admin

                if (admin){
                    return u
                }

                if (json.has("accounts")){
                    val accounts = json["accounts"] as JSONArray
                    for (i in 0 until accounts.length()) {
                        val item = accounts.getJSONObject(i)
                        when {
                            item["accType"] == "Twitter" -> {
                                u.hasTwitter = true
                                u.twitterID = item["id"] as String
                                u.twitterName = item["name"] as String
                            }
                            item["accType"] == "Google" -> {
                                u.hasGoogle = true
                                u.googleID = item["id"] as String
                                u.googleEmail = item["email"] as String
                                u.googleName = item["name"] as String
                            }
                            item["accType"] == "Telegram" -> {
                                u.hasTelegram = true
                                u.telegramChatId = item["chatId"] as Int
                                try{
                                    u.telegramUsername = item["username"] as String
                                }catch(e:Exception){
                                    u.telegramUsername = item["name"] as String
                                }
                            }
                        }
                        // Your code here
                    }
                }

                if (json.has("iamfine")){
                    val iamfines = json["iamfine"] as JSONArray
                    for (i in 0 until iamfines.length()){
                        val iamfine = iamfines.getJSONObject(i)
                        if (iamfine["date"] == LocalDate.now().toString()){
                            u.answeredIAmFineToday = true
                            break
                        }
                    }
                }

                if (json.has("diary")){
                    val diaries = json["diary"] as JSONArray
                    for (i in 0 until diaries.length()){
                        val diary = diaries.getJSONObject(i)
                        u.diaries.add(Diary(diary["datetime"].toString().replace("T", " ").substring(0, 16), diary["diary"].toString()))
                    }
                }

                if (json.has(FRIENDS)){
                    val friends = json[FRIENDS] as JSONArray
                    for (i in 0 until friends.length()){
                        val friend = friends.getJSONObject(i)
                        u.friends.add(Friend(friend[NAME].toString(), friend[EMAIL].toString()))
                    }
                }

                if (json.has(PINGS)){
                    val pings = json[PINGS] as JSONObject
                    if (pings.has(PENDINGPINGS)){
                        val pendings = pings[PENDINGPINGS] as JSONArray
                        for (i in 0 until pendings.length()){
                            val pending = pendings.getJSONObject(i)
                            u.pendingPings.add(PendingPing(pending[NAME].toString(), pending[EMAIL].toString()))
                        }
                    }
                    if (pings.has(PASTPINGS)){
                        val pasts = pings[PASTPINGS] as JSONArray
                        for (i in 0 until pasts.length()){
                            val past = pasts.getJSONObject(i)
                            val fine = past[IAMFINE] == "true"

                            u.pastPings.add(PastPing(past[NAME].toString(), past[EMAIL].toString(), fine, past[DATETIME].toString()))
                        }
                    }
                }

                if (json.has(helpPsychiatrist)){
                    val helpPsy = json[helpPsychiatrist] as JSONObject
                    u.hasHelpPsychiatrist = true
                    u.hPsyEmail = helpPsy[EMAIL].toString()
                    u.hPsyName = helpPsy[NAME].toString()
                    u.hPsyPhoneNo = helpPsy[PHONENO].toString()
                }

                if (json.has(patients)){
                    val patients = json[patients] as JSONArray
                    for (i in 0 until patients.length()){
                        val pat = patients.getJSONObject(i)
                        u.patients.add(Patient(pat[NAME].toString(), pat[EMAIL].toString(), pat[PHONENO].toString()))
                    }
                }

                if (json.has(appointments)){
                    val apps = json[appointments] as JSONArray
                    for (i in 0 until apps.length()){
                        val app = apps.getJSONObject(i)
                        u.appointments.add(Appointment(app[NAME].toString(), app[EMAIL].toString(), app[DATETIME].toString().dropLast(3), app[MEETLINK].toString()))
                    }
                }




                return u
            }
            catch (e: Exception){
                e.printStackTrace()
            }
            return null

        }
    }

    var psychiatristVerified = true
    var admin = false

    var hasTwitter = false
    var twitterID = ""
    var twitterName = ""

    var hasGoogle = false
    var googleID = ""
    var googleEmail = ""
    var googleName = ""

    var hasTelegram = false
    var telegramChatId = 0
    var telegramUsername = ""

    var answeredIAmFineToday = false

    var diaries = ArrayList<Diary>()
    var friends = ArrayList<Friend>()

    var pendingPings = ArrayList<PendingPing>()
    var pastPings = ArrayList<PastPing>()

    var hasHelpPsychiatrist = false
    var hPsyEmail = ""
    var hPsyName = ""
    var hPsyPhoneNo = ""

    var patients = ArrayList<Patient>()

    var isViewingQuestionnaire = false
    var questionnaireAnswer = ""

    var appointments = ArrayList<Appointment>()


    /**
     * Converts this user to JSON. Only used for registering
     */
    fun convertToJson(): JSONObject{
        var jsonStr = "{ '$NAME': '$name', '$EMAIL': '$email', '$USERNAME': '$username', '$PASSWORD': '$password', '$PHONENO': '$phoneNo', '$DOB': '$dob', '$GENDER': '$gender', '$PSYCHIATRIST': '$isPsychiatrist'"
        if (admin){
            jsonStr += ", '$ADMIN': '$admin'"
        }
        jsonStr += " }"
        return JSONObject(jsonStr)
    }

    /**
     * Returns the login data composing of the current email and password
     */
    fun getLoginData(): LoginData {
        return LoginData(email, password)
    }


}

/**
 * Below
 * Data Structures for User
 */
class Diary (val datetime: String, val content: String)
class Friend (val name: String, val email: String)
class PendingPing (val name: String, val email: String)
class PastPing(val name: String, val email: String, val iamfine: Boolean, val dateTime: String)
class Patient(val name: String, val email: String, val phoneno: String)
class Appointment(val name: String, val email: String, val datetime: String, val meetLink: String)
