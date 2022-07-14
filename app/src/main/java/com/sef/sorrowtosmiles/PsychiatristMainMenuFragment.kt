package com.sef.sorrowtosmiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sef.sorrowtosmiles.databinding.FragmentPsychiatristMainmenuBinding


/**
 * Psychiatrist Main Menu Fragment
 */
class PsychiatristMainMenuFragment : Fragment() {

    private var _binding: FragmentPsychiatristMainmenuBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPsychiatristMainmenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override - sets welcoming text
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.instance.hideBackAndSetBackAsExit()

        binding.textViewPsychiatristmmWelcome.text = "Welcome, " + Login.instance.loggedInUser!!.name
        setupButtons()
    }

    /**
     * Setups the buttons to navigate to other menus
     */
    private fun setupButtons(){

        binding.buttonPsychiatristmmPatients.setOnClickListener{
            exitToFragment(R.id.action_psychiatristMainMenuFragment_to_psychiatristPatientsFragment) //TMP
        }
        binding.buttonPsychiatristmmProfile.setOnClickListener{
            exitToFragment(R.id.action_psychiatristMainMenuFragment_to_profileFragment) //TMP
        }
        binding.buttonPsychiatristmmPing.setOnClickListener{
            exitToFragment(R.id.action_psychiatristMainMenuFragment_to_pingFragment) //TMP
        }
        binding.buttonPsychiatristmmFriends.setOnClickListener{
            exitToFragment(R.id.action_psychiatristMainMenuFragment_to_friendsFragment) //TMP
        }
        binding.buttonPsychiatristmmTalkingcircle.setOnClickListener{
            exitToFragment(R.id.action_psychiatristMainMenuFragment_to_talkingCircleFragment) //TMP
        }
        binding.buttonPsychiatristmmAppointments.setOnClickListener{
            exitToFragment(R.id.action_psychiatristMainMenuFragment_to_psychiatristAppointmentsFragment) //TMP
        }
    }

    /**
     * Navigate to other menus
     */
    private fun exitToFragment(id: Int){
        findNavController().navigate(id)
        MainActivity.instance.showRestoreBack()
    }

    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}