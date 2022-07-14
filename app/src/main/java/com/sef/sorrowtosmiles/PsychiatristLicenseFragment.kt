package com.sef.sorrowtosmiles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sef.sorrowtosmiles.databinding.FragmentPsychiatristLicenseBinding

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.cio.*

import android.widget.Toast

import com.android.volley.VolleyError

import org.json.JSONException

import org.json.JSONObject

import com.android.volley.NetworkResponse
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import android.graphics.drawable.Drawable
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import android.R
import com.android.volley.toolbox.ImageLoader

import com.android.volley.toolbox.NetworkImageView
import android.R.string.no
import android.os.Handler
import android.util.LruCache
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response


/**
 * Psychiatrist License Fragment (Registration)
 */
class PsychiatristLicenseFragment : Fragment() {

    private var _binding: FragmentPsychiatristLicenseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var image: ImageView
    private lateinit var bitmap: Bitmap

    val register = RegisterFragment.instance.register

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPsychiatristLicenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = ImageView(context)
        setup()

    }

    /**
     * Setups sending of image to server
     */
    private fun setup(){

        val getFileActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            val data = result.data
            val selectedImage = data?.data

            if (selectedImage != null){

                bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, selectedImage)
                image.setImageBitmap(bitmap)
                binding.textviewPlSelectedfile.text = selectedImage.toString()

            }
        }
        binding.buttonPlSelectfile.setOnClickListener{
            val intent = Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            getFileActivity.launch(intent)
        }

        binding.buttonPlSubmit.setOnClickListener{

            val volleyMultipartRequest: VolleyMultipartRequest = createVolleyMultipartRequest()

            Volley.newRequestQueue(context).add(volleyMultipartRequest)

            val queue = Volley.newRequestQueue(context)
            val imageLoader = ImageLoader(queue, object : ImageLoader.ImageCache {
                private val mCache: LruCache<String, Bitmap> = LruCache<String, Bitmap>(10)
                override fun putBitmap(url: String, bitmap: Bitmap) {
                    mCache.put(url, bitmap)
                }

                override fun getBitmap(url: String): Bitmap? {
                    return mCache.get(url)
                }
            })

            val nv = binding.networkimageUploaded
            nv.setDefaultImageResId(R.drawable.stat_sys_upload) // image for loading...
            binding.networkimageUploaded.setImageUrl("https://www.lifewire.com/thmb/P856-0hi4lmA2xinYWyaEpRIckw=/1920x1326/filters:no_upscale():max_bytes(150000):strip_icc()/cloud-upload-a30f385a928e44e199a62210d578375a.jpg", imageLoader)
            tryShowImage(imageLoader)
        }
    }

    /**
     * Creates a Volley Multipart Request (For image POST)
     */
    private fun createVolleyMultipartRequest() : VolleyMultipartRequest{
        val volleyMultipartRequest: VolleyMultipartRequest =
            object : VolleyMultipartRequest( Method.POST, URLRequest.uploadURL,
                Response.Listener<NetworkResponse?> { response ->
                    try {
                        val obj = JSONObject(String(response!!.data))
                        Toast.makeText(context, obj.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(context, "Network Error", Toast.LENGTH_LONG).show()
                    error.printStackTrace()
                })
            {
                override fun getByteData(): Map<String, DataPart>? {
                    val params: MutableMap<String, DataPart> = HashMap()
                    val username = register.username
                    params["file"] = DataPart(username, getFileDataFromDrawable(bitmap))
                    return params
                }
            }
        return volleyMultipartRequest
    }

    /**
     * Attempt to show an image via an image loader, repeatedly fetch the image until it loads
     */
    private fun tryShowImage(imageLoader: ImageLoader){
        Handler().postDelayed({
            val bind = _binding
            if (bind != null){
                val nv = bind.networkimageUploaded
                nv.setImageUrl(URLRequest.uploadsURL + "/" + register.username, imageLoader)
                tryShowImage(imageLoader)
            }
        }, 1000)
    }

    /**
     * Gets the file data from a drawable
     */
    fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }


    /**
     * Default Override
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}