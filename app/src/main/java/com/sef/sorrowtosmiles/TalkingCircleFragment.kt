package com.sef.sorrowtosmiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sef.sorrowtosmiles.databinding.FragmentTalkingcircleBinding
import org.json.JSONObject

/**
 * Talking Circle Fragment
 */
class TalkingCircleFragment : Fragment() {

    companion object{
        const val command_joinGetTC = "joinGetTC"
        const val email = "email"
        const val googleEmail = "googleEmail"
    }

    private var _binding: FragmentTalkingcircleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTalkingcircleBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButton()

    }

    /**
     * Setups the button to invite the user to the meet and launch meet
     */
    private fun setupButton(){

        binding.buttonMeet.setOnClickListener{
            val u = Login.instance.loggedInUser!!
            val uEmail = u.email
            val uGoogleEmail = u.googleEmail
            val json = JSONObject("{ 'command': '$command_joinGetTC', 'packet': { '$email': '$uEmail', '$googleEmail': '$uGoogleEmail' }}")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                    reply ->
                val link = reply["link"] as String

                val httpIntent = Intent(Intent.ACTION_VIEW)
                println(link)
                httpIntent.data = Uri.parse(link)
                startActivity(httpIntent)
            },{
                    _ ->
                Toast.makeText(context, "Meet Link is not available right now", Toast.LENGTH_LONG).show()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}