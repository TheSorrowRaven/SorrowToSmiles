package com.sef.sorrowtosmiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sef.sorrowtosmiles.databinding.FragmentDiaryBinding
import org.json.JSONObject
import java.time.LocalDateTime
import android.widget.LinearLayout

import android.widget.TextView


/**
 * Diary (User) Fragment
 */
class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrieveRefresh()
        setupButtons()
    }

    /**
     * Setup save diary button
     */
    private fun setupButtons(){
        binding.buttonDiarySave.setOnClickListener{

            sendDiary()
        }
    }

    /**
     * Retrieve data from User and refresh diaries
     */
    private fun retrieveRefresh(){
        val layout = binding.diaryLinearLayout
        layout.removeAllViews()

        for (diary in Login.instance.loggedInUser!!.diaries){

            val date = TextView(context)
            date.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            date.textSize = 20f
            date.text = diary.datetime
            layout.addView(date)

            val text = TextView(context)
            text.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            text.setPadding(8, 0, 8, 0)
            text.text = diary.content
            layout.addView(text)
        }

    }

    /**
     * Sends diary to the server (Save)
     */
    private fun sendDiary(){
        val content = binding.editTextTextMultiLine.text.toString().replace("'", "\\'")
        val dateTime = LocalDateTime.now().toString().substring(0, 19).replace("T", " ")
        val email = Login.instance.loggedInUser!!.email
        val json = JSONObject("{ 'command': 'diary', 'packet': { 'email': '$email', 'datetime': '$dateTime', 'diary': '$content' } }")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            user ->
            val u = User.createUserFromJson(user)
            Login.instance.loggedInUser = u
            retrieveRefresh()
            binding.editTextTextMultiLine.setText("")
        },{

        })
    }

    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}