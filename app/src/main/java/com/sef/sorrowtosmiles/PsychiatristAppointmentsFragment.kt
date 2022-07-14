package com.sef.sorrowtosmiles

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.sef.sorrowtosmiles.databinding.FragmentPsychiatristAppointmentsBinding
import org.json.JSONObject


/**
 * Psychiatrist Appointments Fragment
 */
class PsychiatristAppointmentsFragment : Fragment() {

    companion object {
        const val command_makeAppointmentWith = "makeAppointmentWith"
        const val patientEmail = "patientEmail"
        const val startDateTime = "startDateTime"
        const val endDateTime = "endDateTime"
    }

    private var _binding: FragmentPsychiatristAppointmentsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var patients: ArrayList<Patient>

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPsychiatristAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshAll()
        setup()

    }

    /**
     * Setups necessary components including date and time pickers and create appointment button
     */
    private fun setup(){

        val datePicker = binding.datePickerPsychiatristAppointmentDate
        val timePicker = binding.timePickerPsychiatristAppointmentTime

        val createApp = binding.buttonPsychiatristAppointmentCreate

        createApp.setOnClickListener{

            val year = datePicker.year
            val month = datePicker.month + 1
            val day = datePicker.dayOfMonth
            val hour = timePicker.hour
            var hourStr = hour.toString()
            if (hour < 10){
                hourStr = hourStr.padStart(2, '0')
            }
            val minute = timePicker.minute
            var minuteStr = minute.toString()
            if (hour < 10){
                minuteStr = minuteStr.padStart(2, '0')
            }
            val dateTimeStr = "$year-$month-$day" + "T$hourStr:$minuteStr:00"

            val nextDay = day + 1
            val endDayTimeStr = "$year-$month-$nextDay" + "T$hourStr:$minuteStr:00"

            val radioGroup = binding.radioGroupPsychiatristAppointmentPatientsSelect
            val pIndex = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.checkedRadioButtonId))
            val patient = patients[pIndex]

            val myEmail = Login.instance.loggedInUser!!.email
            val pEmail = patient.email

            val json = JSONObject("{ 'command': '$command_makeAppointmentWith', 'packet': { 'email': '$myEmail', '$patientEmail': '$pEmail', '$startDateTime': '$dateTimeStr', '$endDateTime': '$endDayTimeStr' }}")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                Toast.makeText(context, "Appointment Created", Toast.LENGTH_LONG).show()
                Handler().postDelayed({
                    refreshUserAll()
                    Toast.makeText(context, "Appointments Refreshed", Toast.LENGTH_LONG).show()
                }, 1000)
            },{
                    err ->
                println(err)
            })

        }
    }

    /**
     * Refreshes upcoming appointments, and the patients available to make appointments with
     */
    private fun refreshAll(){
        refreshUpcoming()
        refreshPatients()
    }

    /**
     * Refreshes the User (Psychiatrist) by requesting from the server again
     */
    private fun refreshUserAll(){
        val email = Login.instance.loggedInUser!!.email
        val json = JSONObject("{ 'command': 'refresh', 'packet': { 'email': '$email' }}")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            result ->
                Login.instance.tryCreateUserLoginNoCallback(result)
                refreshAll()
        },{

        })
    }

    /**
     * Refreshes the available patients
     */
    private fun refreshPatients(){
        val radioGroup = binding.radioGroupPsychiatristAppointmentPatientsSelect
        radioGroup.removeAllViews()

        val u = Login.instance.loggedInUser!!
        patients = u.patients
        for (p in patients){
            val radio = RadioButton(context)
            radio.layoutParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            radio.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.main_general_text))
            radio.setTextColor(resources.getColor(R.color.main_general_text))
            radio.text = p.name
            radioGroup.addView(radio)
        }

    }

    /**
     * Refreshes upcoming appointments
     */
    private fun refreshUpcoming(){
        val layout = binding.linearLayoutUpcoming
        layout.removeAllViews()

        val u = Login.instance.loggedInUser!!
        val apps = u.appointments

        for (app in apps){
            val text = TextView(context)
            text.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            text.textAlignment = View.TEXT_ALIGNMENT_CENTER
            text.text = "Session w/" + app.name

            val horizontalLayout = LinearLayout(context)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL
            horizontalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

            val button = Button(context)
            button.text = "MEET LINK"
            button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            button.setOnClickListener{
                if (app.meetLink == ""){
                    Toast.makeText(context, "Check back later for meet link", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val httpIntent = Intent(Intent.ACTION_VIEW)
                println(app.meetLink)
                httpIntent.data = Uri.parse(app.meetLink)
                startActivity(httpIntent)
            }

            val date = TextView(context)
            date.text = app.datetime
            date.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

            layout.addView(text)

            horizontalLayout.addView(button)
            horizontalLayout.addView(date)

            layout.addView(horizontalLayout)

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