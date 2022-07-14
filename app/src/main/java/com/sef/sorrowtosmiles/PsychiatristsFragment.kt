package com.sef.sorrowtosmiles

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.sef.sorrowtosmiles.databinding.FragmentPsychiatristsBinding
import org.json.JSONArray
import org.json.JSONObject


/**
 * Psychiatrists Fragment (User)
 */
class PsychiatristsFragment : Fragment() {

    companion object{
        const val command_fetchAllPsychiatrists = "fetchAllPsychiatrists"
        const val command_requestPsychiatrist = "requestPsychiatrist"
    }

    private var _binding: FragmentPsychiatristsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPsychiatristsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh()

    }

    /**
     * Refreshes the psychiatrist
     */
    private fun refresh(){
        val linearLayout = binding.linearLayoutPsychiatristDisplay
        linearLayout.removeAllViews()

        val u = Login.instance.loggedInUser!!
        if (u.hasHelpPsychiatrist){

            showPsychiatrist(u.hPsyName, u.hPsyEmail, u.hPsyPhoneNo, linearLayout, false)

            return
        }

        val json = JSONObject("{ 'command': '$command_fetchAllPsychiatrists'}")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            result ->
            val users = result["users"] as JSONArray
            for (i in 0 until users.length()){
                val user = users[i] as JSONObject
                showPsychiatrist(user["name"].toString(), user["email"].toString(), user["phoneno"].toString(), linearLayout)
            }
        },{

        })

    }

    /**
     * Shows each psychiatrist in a proper layout, also setup to request psychiatrist
     */
    private fun showPsychiatrist(pname: String, pemail: String, pphoneno: String, linearLayout: LinearLayout, hasButton: Boolean = true){

        val cardView = CardView(requireContext())
        cardView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        cardView.setCardBackgroundColor(resources.getColor(R.color.main_focal_button_background))

        val verticalLayout = LinearLayout(context)
        verticalLayout.orientation = LinearLayout.VERTICAL
        verticalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        val name = TextView(context)
        var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.marginStart = 8
        name.setTextColor(resources.getColor(R.color.main_focal_button_text))
        name.text = pname
        name.layoutParams = layoutParams
        name.textSize = 24f
        verticalLayout.addView(name)

        val number = TextView(context)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.marginStart = 128
        layoutParams.topMargin = 8
        layoutParams.marginEnd = 8
        number.layoutParams = layoutParams
        number.text = pphoneno
        number.textSize = 18f
        number.setTextColor(resources.getColor(R.color.main_focal_button_text))
        verticalLayout.addView(number)

        if (hasButton){
            val button = Button(context)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            button.layoutParams = layoutParams
            button.text = "Request $pname"

            button.setOnClickListener{
                val myEmail = Login.instance.loggedInUser!!.email
                val json = JSONObject("{ 'command': '$command_requestPsychiatrist', 'packet': {'email': '$myEmail', 'otherEmail': '$pemail'}}")
                URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                        result ->
                    Login.instance.tryCreateUserLoginNoCallback(result)
                    Toast.makeText(context, "Successfully added psychiatrist", Toast.LENGTH_LONG).show()
                    refresh()
                },{

                })
            }
            verticalLayout.addView(button)
        }

        cardView.addView(verticalLayout)
        linearLayout.addView(cardView)

    }

    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}