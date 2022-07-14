package com.sef.sorrowtosmiles

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.LruCache
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.Volley
import com.sef.sorrowtosmiles.databinding.FragmentAdminMainmenuBinding
import org.json.JSONArray
import org.json.JSONObject
import android.view.Gravity


/**
 * Admin Main Menu Fragment
 */
class AdminMainMenuFragment : Fragment() {

    companion object{
        const val COMMAND = "command"
        const val PACKET = "packet"
        const val USERS = "users"
        const val EMAIL = "email"
        const val NAME = "name"
        const val command_fetchUnverifiedPsychiatrists = "fetchUnverifiedPsychiatrists"
        const val command_verifyUser = "verifyUser"
        const val command_denyUser = "denyUser"
        const val command_deleteUser = "deleteUser"
        const val command_fetchAllUsers = "fetchAllUsers"
    }


    private var _binding: FragmentAdminMainmenuBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminMainmenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override - After view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MainActivity.instance.hideBackAndSetBackAsExit()

        setupButtons()
        refreshAll()
    }

    /**
     * Setup static buttons - Logout
     */
    private fun setupButtons(){
        binding.buttonAdminLogout.setOnClickListener{
            exitToFragment(R.id.action_adminMainMenuFragment_to_LoginFragment)
        }
    }


    /**
     * Refresh Licenses and Users by querying server
     */
    private fun refreshAll(){
        refreshLicenses()
        refreshUsers()
    }

    /**
     * Refreshes Licenses if available
     */
    private fun refreshLicenses(){

        val linearLayout = binding.linearLayoutLicenseContainer
        linearLayout.removeAllViews()

        val json = JSONObject("{ '$COMMAND': '$command_fetchUnverifiedPsychiatrists'}")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            result ->
                val users = result[USERS] as JSONArray
                if (users.length() == 0){
                    Toast.makeText(context, "There are no psychiatrists to verify", Toast.LENGTH_LONG).show()
                    return@jsonPOSTRequest
                }
                for (i in 0 until users.length()){
                    val user = users[i]
                    displayLicense(linearLayout, user as JSONObject)
                }
        },{
            err ->
                Toast.makeText(context, "Server unavailable right now", Toast.LENGTH_LONG).show()
                println(err)
        })

    }

    /**
     * Display each license in a standard layout
     */
    private fun displayLicense(linearLayout: LinearLayout, psy: JSONObject){
        val card = CardView(requireContext())
        val verticalLayout = LinearLayout(context)
        verticalLayout.orientation = LinearLayout.VERTICAL
        verticalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        val name = TextView(context)
        name.text = psy[NAME].toString()
        name.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        name.textSize = 20f
        name.typeface = Typeface.DEFAULT_BOLD


        val queue = Volley.newRequestQueue(context)

        /**
         * Image Loader here for loading images from network
         */
        val imageLoader = ImageLoader(queue, object : ImageLoader.ImageCache {
            private val mCache: LruCache<String, Bitmap> = LruCache<String, Bitmap>(10)
            override fun putBitmap(url: String, bitmap: Bitmap) {
                mCache.put(url, bitmap)
            }

            override fun getBitmap(url: String): Bitmap? {
                return mCache.get(url)
            }
        })

        val nv = NetworkImageView(context)
        nv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        nv.adjustViewBounds = true
        nv.scaleType = ImageView.ScaleType.FIT_CENTER
        nv.setDefaultImageResId(android.R.drawable.stat_sys_upload) // image for loading...
        nv.setImageUrl("https://www.lifewire.com/thmb/P856-0hi4lmA2xinYWyaEpRIckw=/1920x1326/filters:no_upscale():max_bytes(150000):strip_icc()/cloud-upload-a30f385a928e44e199a62210d578375a.jpg", imageLoader)
        nv.setErrorImageResId(android.R.drawable.stat_sys_upload)
        tryShowImage(imageLoader, nv, psy["username"].toString())

        val horizontalLayout = LinearLayout(context)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        horizontalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layoutParams.marginEnd = 20
        val acceptButton = Button(context)
        acceptButton.layoutParams = layoutParams
        acceptButton.text = "Accept"
        acceptButton.setOnClickListener{acceptDenyUser(psy[EMAIL].toString(), true)}

        val denyButton = Button(context)
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        denyButton.layoutParams = layoutParams
        denyButton.text = "Deny"
        denyButton.setOnClickListener{acceptDenyUser(psy[EMAIL].toString(), false)}
        denyButton.setBackgroundColor(resources.getColor(R.color.invisible))

        horizontalLayout.addView(acceptButton)
        horizontalLayout.addView(denyButton)

        verticalLayout.addView(name)
        verticalLayout.addView(nv)
        verticalLayout.addView(horizontalLayout)
        card.addView(verticalLayout)

        linearLayout.addView(card)

    }

    /**
     * Constantly Refresh the image display to make sure it shows
     */
    private fun tryShowImage(imageLoader: ImageLoader, nv: NetworkImageView, username: String){
        Handler().postDelayed({
            val bind = _binding
            if (bind != null){
                nv.setImageUrl(URLRequest.uploadsURL + "/" + username, imageLoader)
                tryShowImage(imageLoader, nv, username)
            }
        }, 1000)
    }

    /**
     * Accept or deny psychiatrist and notify server
     */
    private fun acceptDenyUser(email: String, accept: Boolean){
        if (accept){
            val json = JSONObject("{ '$COMMAND': '$command_verifyUser', '$PACKET': { 'email': '$email' }}")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                result ->
                    Toast.makeText(context, result["message"].toString(), Toast.LENGTH_LONG).show()
            },{
                err ->
                    println(err)
            })
        }
        else{
            val json = JSONObject("{ '$COMMAND': '$command_denyUser', '$PACKET': { 'email': '$email' } }")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                result ->
                    Toast.makeText(context, result["message"].toString(), Toast.LENGTH_LONG).show()
            },{
                err ->
                    println(err)
            })
        }
        refreshAll()
    }

    /**
     * Refresh Users list for deletion
     */
    private fun refreshUsers(){

        val linearLayout = binding.linearLayoutDeleteUsers
        linearLayout.removeAllViews()

        val json = JSONObject("{ 'command': '$command_fetchAllUsers'}")
        URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
            result ->
                val users = result["users"] as JSONArray
                for (i in 0 until users.length()){
                    val user = users[i]
                    displayUser(linearLayout, user as JSONObject)
                }
        },{
            err ->
                Toast.makeText(context, "Server unavailable right now", Toast.LENGTH_LONG).show()
                println(err)
        })
    }

    /**
     * Display each user with a standard layout
     */
    private fun displayUser(linearLayout: LinearLayout, json: JSONObject){

        val horizontalLayout = LinearLayout(context)
        horizontalLayout.orientation = LinearLayout.HORIZONTAL
        horizontalLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)


        var layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layoutParams.gravity = Gravity.CENTER_VERTICAL

        val text = TextView(context)
        text.text = json["name"].toString() + "\n" + json["email"].toString()
        text.textSize = 18f
        text.layoutParams = layoutParams


        layoutParams = LinearLayout.LayoutParams(64, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.RIGHT
        val button = ImageButton(context)
        button.layoutParams = layoutParams
        button.setImageDrawable(resources.getDrawable(R.drawable.delete_icon))
        button.setBackgroundColor(resources.getColor(R.color.invisible))
        button.adjustViewBounds = true
        button.scaleType = ImageView.ScaleType.FIT_CENTER
        button.setOnClickListener{
            deleteUser(json["name"].toString(), json["email"].toString())
        }

        horizontalLayout.addView(text)
        horizontalLayout.addView(button)

        linearLayout.addView(horizontalLayout)


    }

    /**
     * Confirms delete user action
     */
    private fun deleteUser(name: String, email: String){

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle("Confirm Delete $name")
        builder.setMessage("The user will be *PERMANENTLY* deleted and cannot be recovered")
        builder.setPositiveButton("Confirm"
        ) {
            dialog, _ ->
            val json = JSONObject("{ 'command': '$command_deleteUser', 'packet': { 'email': '$email' }}")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                result ->
                    Toast.makeText(context, result["message"].toString(), Toast.LENGTH_LONG).show()
                    refreshUsers()
                    dialog.dismiss()
            },{
                err ->
                    println(err)
                    dialog.dismiss()
            })
        }
        builder.setNegativeButton(android.R.string.cancel
        ) { dialog, _ -> dialog.dismiss() }

        val dialog: AlertDialog = builder.create()
        dialog.show()

    }


    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Defaykt Override
     */
    private fun exitToFragment(id: Int){
        findNavController().navigate(id)
        MainActivity.instance.showRestoreBack()
    }

}