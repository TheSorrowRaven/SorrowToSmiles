package com.sef.sorrowtosmiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sef.sorrowtosmiles.databinding.FragmentHelpBinding
import android.content.Intent
import android.net.Uri
import androidx.navigation.fragment.findNavController


/**
 * Help (User) Fragment
 */
class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
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
     * Setups buttons for calling, psychiatrists and diary
     */
    private fun setupButtons(){

        val emergencyCall = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "999"))
            startActivity(intent)
        }
        val hotline1Call = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "0376272929"))
            startActivity(intent)
        }
        val hotline2Call = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "06392850039"))
            startActivity(intent)
        }
        binding.buttonEmergencynumber.setOnClickListener{emergencyCall()}
        binding.imageButtonEmergency.setOnClickListener{emergencyCall()}
        binding.buttonHotline1.setOnClickListener{hotline1Call()}
        binding.imageButtonHotline1.setOnClickListener{hotline1Call()}
        binding.buttonHotline2.setOnClickListener{hotline2Call()}
        binding.imageButtonHotline2.setOnClickListener{hotline2Call()}

        binding.buttonHelpFindpsychiatrists.setOnClickListener{
            findNavController().navigate(R.id.action_helpFragment_to_psychiatristsFragment)
        }
        binding.buttonHelpFindDiary.setOnClickListener{
            findNavController().navigate(R.id.action_helpFragment_to_diaryFragment)
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