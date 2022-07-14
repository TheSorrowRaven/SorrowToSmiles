package com.sef.sorrowtosmiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.sef.sorrowtosmiles.databinding.FragmentAppointmentsBinding


/**
 * Appointments (User) Fragment
 */
class AppointmentsFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh()
        setupTexts()
    }

    /**
     * Setups static texts
     */
    private fun setupTexts(){
        val u = Login.instance.loggedInUser!!
        val psyName = u.hPsyName
        if (psyName == ""){
            binding.textViewAppointmentPsychiatrist.text = ""
            binding.textViewAppointmentDesc.text = "You don't have a psychiatrist yet"
        }
        else{
            binding.textViewAppointmentPsychiatrist.text = "w/ $psyName"
        }
    }

    /**
     * Refreshes available appointments/upcoming appointments
     */
    private fun refresh(){
        val layout = binding.linearLayoutAppointments
        layout.removeAllViews()

        val u = Login.instance.loggedInUser!!
        val apps = u.appointments

        for (app in apps){

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