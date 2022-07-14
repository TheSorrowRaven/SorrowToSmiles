package com.sef.sorrowtosmiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.sef.sorrowtosmiles.databinding.FragmentMainmenuBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 *
 * Main Menu (User) Fragment
 */
class MainMenuFragment : Fragment() {

    private var _binding: FragmentMainmenuBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val login: Login by lazy { Login.instance }

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainmenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var showText: TextView

    /**
     * Default Override - Setup welcome text
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.instance.hideBackAndSetBackAsExit()

        val user = login.loggedInUser!!

        binding.textViewWelcome.text = "Welcome, " + user.name

        setupButtons()

    }

    /**
     * Setups redirection buttons
     */
    private fun setupButtons(){

        binding.buttonProfile.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_profileFragment) //TMP
        }
        binding.buttonPsychiatrists.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_psychiatristsFragment) //TMP
        }
        binding.buttonPing.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_pingFragment) //TMP
        }
        binding.buttonHelp.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_helpFragment) //TMP
        }

        binding.buttonIamfine.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_IAmFineFragment) //TMP
        }
        binding.buttonDiary.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_diaryFragment) //TMP
        }
        binding.buttonQuestionnaire.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_questionnaireFragment) //TMP
        }
        binding.buttonFriends.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_friendsFragment) //TMP
        }
        binding.buttonTalkingcircle.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_talkingCircleFragment) //TMP
        }
        binding.buttonAppointments.setOnClickListener{
            exitToFragment(R.id.action_MainMenuFragment_to_appointmentsFragment) //TMP
        }
    }

    /**
     * Redirect to other fragment
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