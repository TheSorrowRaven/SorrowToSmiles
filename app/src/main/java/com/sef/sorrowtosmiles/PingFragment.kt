package com.sef.sorrowtosmiles

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.sef.sorrowtosmiles.databinding.FragmentPingBinding
import org.json.JSONObject
import java.time.LocalDateTime
import kotlin.math.roundToInt


/**
 * Ping Fragment
 */
class PingFragment : Fragment() {

    companion object{
        const val command_pingFriend = "pingFriend"
        const val command_replyPing = "replyPing"

    }

    private var _binding: FragmentPingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPingBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshAll()

    }

    /**
     * Refresh All Elements
     */
    private fun refreshAll(){
        refreshPendingPingsList()
        refreshToPingList()
        refreshPastPingsList()
    }

    /**
     * Refresh Past Pings (previous replied pings)
     */
    private fun refreshPastPingsList(){

        val linearLayout = binding.linearLayoutPastPings
        linearLayout.removeAllViews()
        val user = Login.instance.loggedInUser!!
        val pings = user.pastPings

        for (ping in pings){

            val text = TextView(context)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics).roundToInt()
            text.layoutParams = layoutParams
            text.textAlignment = View.TEXT_ALIGNMENT_CENTER

            var fine = "I Am Fine"
            if (!ping.iamfine){
                fine = "I Am Not Fine"
            }
            text.text = ping.name + " replied with \"" + fine + "\n" + ping.dateTime

            linearLayout.addView(text)

        }

    }

    /**
     * Refreshes this user's pending pings (to ping)
     */
    private fun refreshPendingPingsList(){
        val linearLayout = binding.linearLayoutPendingPings
        linearLayout.removeAllViews()
        val user = Login.instance.loggedInUser!!
        val pings = user.pendingPings

        for (ping in pings){

            val verticalLayout = LinearLayout(context)
            verticalLayout.orientation = LinearLayout.VERTICAL
            verticalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

            val name = TextView(context)
            name.text = ping.name
            name.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            name.textSize = 16f
            name.textAlignment = View.TEXT_ALIGNMENT_CENTER
            name.setBackgroundColor(resources.getColor(R.color.main_focal_button_text))

            val horizontalLayout = LinearLayout(context)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            horizontalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

            val fineButton = Button(context)
            fineButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            fineButton.text = "I AM FINE"
            fineButton.setOnClickListener{
                replyPing(user.email, ping.email, true)
            }

            val notFineButton = Button(context)
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            layoutParams.marginStart = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).roundToInt()
            notFineButton.text = "I AM NOT FINE"
            notFineButton.layoutParams = layoutParams
            notFineButton.setOnClickListener{
                replyPing(user.email, ping.email, false)
            }

            horizontalLayout.addView(fineButton)
            horizontalLayout.addView(notFineButton)

            verticalLayout.addView(name)
            verticalLayout.addView(horizontalLayout)

            linearLayout.addView(verticalLayout)

        }

    }

    /**
     * Reply a ping with i am fine or not to send to server
     */
    private fun replyPing(email: String, otherEmail: String, iamfine: Boolean){
        val dateTime = LocalDateTime.now().toString().substring(0, 19).replace("T", " ")
        val json = JSONObject("{ 'command': '$command_replyPing', 'packet': { 'email': '$email', 'otherEmail': '$otherEmail', 'iamfine': '$iamfine', 'datetime': '$dateTime' }}")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            reply ->
                Login.instance.tryCreateUserLoginNoCallback(reply)
                Toast.makeText(context, "Ping Replied", Toast.LENGTH_LONG).show()
                refreshAll()
                if (!Login.instance.loggedInUser!!.isPsychiatrist && !iamfine){
                    findNavController().navigate(R.id.action_pingFragment_to_helpFragment)
                }
        },{
            err ->
                println(err)
        })
    }


    /**
     * Refreshes who to ping list (friends)
     */
    private fun refreshToPingList(){
        val linearLayout = binding.linearLayoutToPingList
        linearLayout.removeAllViews()
        val user = Login.instance.loggedInUser!!
        val friends = user.friends

        for (friend in friends){

            val button = Button(context)
            button.text = friend.name
            button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            button.textSize = 16f
            button.background = resources.getDrawable(R.drawable.texture_rectangle_round)
            button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_sub_button_background))
            button.setTextColor(resources.getColor(R.color.main_sub_button_text))
            button.typeface = Typeface.DEFAULT_BOLD

            button.setOnClickListener{
                val email = user.email
                val otherEmail = friend.email
                val json = JSONObject("{ 'command': '$command_pingFriend', 'packet': { 'email': '$email', 'otherEmail': '$otherEmail' }}")
                URLRequest.jsonPOSTRequest(URLRequest.serverURL, json,{
                    result ->
                        Toast.makeText(context, result["message"].toString(), Toast.LENGTH_LONG).show()
                },{
                    err ->
                        println(err)
                })
            }

            linearLayout.addView(button)

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