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
import com.sef.sorrowtosmiles.databinding.FragmentPsychiatristPatientsBinding
import org.json.JSONObject


/**
 * Psychiatrist Patients Fragment
 */
class PsychiatristPatientsFragment : Fragment() {

    companion object{
        const val answer = "answer"
        const val datetime = "datetime"
        const val command_viewQuestionnaire = "viewQuestionnaire"
    }

    private var _binding: FragmentPsychiatristPatientsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPsychiatristPatientsBinding.inflate(inflater, container, false)
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
     * Refreshes the patients list
     */
    private fun refresh(){
        val linearLayout = binding.linearLayoutPatients
        linearLayout.removeAllViews()
        val u = Login.instance.loggedInUser!!
        val patients = u.patients
        for (p in patients){

            showPatient(p, linearLayout, u)

        }

    }

    /**
     * Shows a single patient in a card view, and setups their questionnaires display
     */
    private fun showPatient(p: Patient, linearLayout: LinearLayout, u: User){

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
        name.text = p.name
        name.layoutParams = layoutParams
        name.textSize = 24f
        verticalLayout.addView(name)

        val number = TextView(context)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.marginStart = 128
        layoutParams.topMargin = 8
        layoutParams.marginEnd = 8
        number.layoutParams = layoutParams
        number.text = p.phoneno
        number.textSize = 18f
        number.setTextColor(resources.getColor(R.color.main_focal_button_text))
        verticalLayout.addView(number)

        val button = Button(context)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER
        button.layoutParams = layoutParams
        button.text = "View Questionnaires"

        button.setOnClickListener{
            val email = p.email
            val json = JSONObject("{ 'command': '$command_viewQuestionnaire', 'packet': {'email': '$email' }}")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                result ->

                    if (result.has("message")){
                        Toast.makeText(context, result["message"].toString(), Toast.LENGTH_LONG).show()
                        return@jsonPOSTRequest
                    }

                    u.isViewingQuestionnaire = true
                    u.questionnaireAnswer = result[answer].toString()
                    Toast.makeText(context, "Viewing Questionnaire Answered At ${result[datetime]}", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_psychiatristPatientsFragment_to_questionnaireFragment)
            },{

            })
        }
        verticalLayout.addView(button)

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