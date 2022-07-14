package com.sef.sorrowtosmiles

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.sef.sorrowtosmiles.databinding.FragmentRegisterBinding
import java.time.LocalDate
import java.time.Period
import android.telephony.PhoneNumberUtils
import android.widget.*
import com.android.volley.VolleyError
import java.time.temporal.ChronoField
import java.util.*


/**
 * Registration Fragment
 */
class RegisterFragment : Fragment() {

    companion object{

        lateinit var instance: RegisterFragment

        /**
         * Returns the LocalDate from a date picker
         */
        private fun getDateFromDatePicker(datePicker: DatePicker): LocalDate {
            val year = datePicker.year
            val month = datePicker.month + 1
            val day = datePicker.dayOfMonth
            return LocalDate.of(year, month, day)
        }

        /**
         * Returns the current date
         */
        private fun getCurrentDate(): LocalDate{
            return LocalDate.now()
        }

        /**
         * Returns if a phone number is valid
         */
        private fun phoneNoValid(phoneNo: String): Boolean{
            return PhoneNumberUtils.isGlobalPhoneNumber(phoneNo)
        }
    }

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var register: Register



    private lateinit var usernameET: EditText
    private lateinit var nameET: EditText
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var confirmPasswordET: EditText
    private lateinit var phoneNoET: EditText
    private lateinit var dobDatePicker: DatePicker
    private lateinit var genderRadio: RadioGroup
    private lateinit var passwordStatusView: TextView

    private var password: String = ""
    private var confirmPassword: String = ""

    private var emailValid: Boolean = false
    private var passwordConfirmValid: Boolean = false
    private var passwordLongEnough = false
    private var phoneNoValid = false
    private var genderChecked = false
    private val canRegister get() = emailValid && passwordConfirmValid && passwordLongEnough && phoneNoValid && genderChecked

    private var editTextOriginalTint: ColorStateList? = null
    private var editTextErrorTint: ColorStateList? = null


    /**
     * Default Override
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root

    }

    /**
     * Default Override - Initialize Register
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instance = this
        register = Register(this)

        setup()

    }

    /**
     * Setups the editTexts, buttons
     */
    private fun setup(){

        //Init fields
        usernameET = binding.registerUsername
        nameET = binding.registerDisplayname
        emailET = binding.registerEmail
        passwordET = binding.registerPassword
        confirmPasswordET = binding.registerConfirmpassword
        phoneNoET = binding.registerPhoneno
        dobDatePicker = binding.datePickerDob
        genderRadio = binding.radioGroupGender
        passwordStatusView = binding.textViewPasswordstatus

        //Init
        editTextOriginalTint = usernameET.backgroundTintList
        val len = LoginData.STRONG_PASSWORD_LENGTH
        passwordStatusView.text = "Password must be at least $len characters"
        dobDatePicker.maxDate = Calendar.getInstance().timeInMillis
        editTextErrorTint = requireContext().getColorStateList(R.color.errorColor)

        //Init buttons
        binding.buttonRegisterRegisterpsychiatrist.setOnClickListener{
            isRegisteringAsPsychiatrist = true
            registerClick(true)
        }
        binding.registerEmail.addTextChangedListener {
                edit ->
            emailChanged(edit)
        }
        binding.registerPassword.addTextChangedListener{
                edit ->
            password = edit.toString()
            passwordChangedCheck()
        }
        binding.registerConfirmpassword.addTextChangedListener{
                edit ->
            confirmPassword = edit.toString()
            passwordChangedCheck()
        }
        binding.registerPhoneno.addTextChangedListener{
                edit ->
            phoneNoChanged(edit)
        }
        binding.radioGroupGender.setOnCheckedChangeListener {
                genderGroup, i ->
            genderChecked()
        }
        binding.buttonRegister.setOnClickListener{
            registerClick(false)
        }
    }

    /**
     * When email is changed check email
     */
    private fun emailChanged(edit: Editable?){
        emailValid = LoginData.isValidEmail(edit.toString())
        setEditTextValidity(emailET, emailValid)
    }

    /**
     * When password is changed check the password
     */
    private fun passwordChangedCheck(){

        passwordLongEnough = LoginData.isValidPassword(password)
        passwordStatusView.visibility = if (passwordLongEnough) TextView.INVISIBLE else TextView.VISIBLE
        setEditTextValidity(passwordET, passwordLongEnough)

        passwordConfirmValid = password == confirmPassword
        setEditTextValidity(confirmPasswordET, passwordConfirmValid)
    }

    /**
     * When the phone no is changed check the phone number
     */
    private fun phoneNoChanged(edit: Editable?){
        phoneNoValid = phoneNoValid(edit.toString())
        setEditTextValidity(phoneNoET, phoneNoValid)
    }

    /**
     * Display error (make red) of the Invalid texts
     */
    private fun setEditTextValidity(editText: EditText, valid: Boolean){
        if (valid){
            editText.backgroundTintList = editTextOriginalTint
            return
        }
        editText.backgroundTintList = editTextErrorTint
    }

    /**
     * Make sure gender is ticked
     */
    private fun genderChecked(){
        genderChecked = true
    }


    /**
     * When register is clicked (register user)
     */
    private fun registerClick(isPsychiatrist: Boolean){
        if (!canRegister){
            return
        }
        val username = usernameET.text.toString()
        val name = nameET.text.toString()
        val email = emailET.text.toString()
        val password = passwordET.text.toString()
        val phoneNo = phoneNoET.text.toString()
        val dob = getDateFromDatePicker(dobDatePicker)

        val index: Int = genderRadio.indexOfChild(genderRadio.findViewById(genderRadio.checkedRadioButtonId))
        var gender = 'O'
        when (index){
            0 -> gender = 'F'
            1 -> gender = 'M'
            2 -> gender = 'O'
        }

        val user = User(name, email, username, password, phoneNo, dob, gender, isPsychiatrist)

        register.registerUser(user)
    }

    var isRegisteringAsPsychiatrist = false

    /**
     * If register is successful, go to the next fragment (login, or license)
     */
    fun registerSuccessful(user: User){
        Toast.makeText(context, "Registration is Successful", Toast.LENGTH_LONG).show()
        if (isRegisteringAsPsychiatrist){
            findNavController().navigate(R.id.action_register_to_psychiatristlicense)
        }
        else{
            val login = Login.instance
            login.setOnSuccess{
                _ ->
                    findNavController().navigate(R.id.action_register_to_login)
            }
            login.tryLogin(user.getLoginData())
        }
    }

    /**
     * Display the registration error
     */
    fun registerError(err: String){
        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
    }

    /**
     * Display the network error
     */
    fun registerVolleyError(err: VolleyError){
        Toast.makeText(context, err.message.toString(), Toast.LENGTH_LONG).show()
    }

    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}