package com.sef.sorrowtosmiles

import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.sef.sorrowtosmiles.databinding.FragmentFriendsBinding
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Friends Fragment
 */
class FriendsFragment : Fragment() {

    companion object{
        const val command_addFriend=  "addFriend"
        const val otherEmail = "otherEmail"

    }

    private var _binding: FragmentFriendsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshFriendsList()

        setup()

    }

    /**
     * Setups editText reference and the add friends button
     */
    private fun setup(){

        val editText = binding.editTextFriendsAdd
        binding.imageButtonFriendsAdd.setOnClickListener{

            val emailText = editText.text
            val myEmail = Login.instance.loggedInUser!!.email
            val json = JSONObject("{ 'command': '$command_addFriend', 'packet': { 'email': '$myEmail', '$otherEmail': '$emailText' }}")

            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                    reply ->
                if (reply.has("error")){
                    Toast.makeText(context, reply["error"] as String, Toast.LENGTH_LONG).show()
                    return@jsonPOSTRequest
                }
                Login.instance.tryCreateUserLoginNoCallback(reply)
                refreshFriendsList()
            },{
                    err ->
                println(err)
                Toast.makeText(context, err.message.toString(), Toast.LENGTH_LONG).show()
            })


        }
    }

    /**
     * Refresh Friends List from User
     */
    private fun refreshFriendsList(){
        val layout = binding.linearlayoutFriends
        layout.removeAllViews()

        val user = Login.instance.loggedInUser!!
        val email = user.email

        for (friend in user.friends){

            val horizontalLayout = LinearLayout(context)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL

            val deleteButton = ImageButton(context)
            val nameText = TextView(context)

            val friendEmail = friend.email
            val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).roundToInt();
            deleteButton.layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            val topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).roundToInt();
            deleteButton.setPadding(0, topMargin, 0, 0)
            deleteButton.setImageResource(R.drawable.delete_icon)
            deleteButton.scaleType = ImageView.ScaleType.FIT_CENTER
            deleteButton.adjustViewBounds = true
            deleteButton.setOnClickListener{
                val json = JSONObject("{ 'command': 'deleteFriend', 'packet': { 'email': '$email', '$otherEmail': '$friendEmail' }}")
                URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                    reply ->
                        Login.instance.tryCreateUserLoginNoCallback(reply)
                        refreshFriendsList()
                },
                {

                })
            }

            nameText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            nameText.text = friend.name

            horizontalLayout.addView(deleteButton)
            horizontalLayout.addView(nameText)

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