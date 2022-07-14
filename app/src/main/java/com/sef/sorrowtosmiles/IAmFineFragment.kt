package com.sef.sorrowtosmiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sef.sorrowtosmiles.databinding.FragmentIAmFineBinding
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.*


/**
 * I AM FINE (User) Fragment
 */
class IAmFineFragment : Fragment() {

    companion object{
        const val IAMFINE = "iamfine"
        const val QUOTESURL = "https://type.fit/api/quotes"
    }

    private var _binding: FragmentIAmFineBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIAmFineBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
    }

    /**
     * Setups the I am fine and I am NOT fine buttons
     */
    private fun setupButtons(){

        binding.buttonIamfineIamfine.setOnClickListener{
            iAmFine(true)
            URLRequest.requestGET(QUOTESURL, {
                    quotesStr ->
                /**
                 * Fetches motivational quotes
                 */
                val quotesJson = JSONArray(quotesStr)
                val random = (0 until quotesJson.length()).random()
                val quote = quotesJson.get(random) as JSONObject
                binding.textViewIamfineDescription.text = quote["text"].toString() + " - by " + quote["author"]
            },{

            })
        }
        binding.buttonIamfineIamnotfine.setOnClickListener{
            iAmFine(false)
            findNavController().navigate(R.id.action_IAmFineFragment_to_helpFragment)
        }
    }

    /**
     * Sends I Am Fine to the server
     */
    fun iAmFine(isFine: Boolean){
        val u = Login.instance.loggedInUser!!
        val email = u.email
        val date = LocalDate.now()
        val json = JSONObject("{ 'command': '$IAMFINE', 'packet': { 'email': '$email', 'iamfine': $isFine, 'date': '$date' } }")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            _ ->

        },{

        })
        u.answeredIAmFineToday = true
    }


    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}