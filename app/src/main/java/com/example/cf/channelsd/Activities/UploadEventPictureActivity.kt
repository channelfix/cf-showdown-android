package com.example.cf.channelsd.Activities

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.cf.channelsd.Data.Reply
import com.example.cf.channelsd.Interfaces.EventInterface
import com.example.cf.channelsd.R
import com.example.cf.channelsd.Retrofit.ProgressRequestBody
import com.example.cf.channelsd.Utils.ApiUtils
import com.example.cf.channelsd.Utils.ImageFilePath
import com.example.cf.channelsd.Utils.picasso
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import kotlinx.android.synthetic.main.activity_upload_event_photo.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadEventPictureActivity : AppCompatActivity(), ProgressRequestBody.UploadCallbacks {

    private var progressBar: ProgressBar? = null
    private val eventInterface: EventInterface = ApiUtils.apiEvent
    private val RESULT_LOAD_IMAGE = 1
    private lateinit var filePartImage: RequestBody
    private var fileImage: MultipartBody.Part? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_event_photo)
        progressBar = progressBarEventPhoto
        progressBar!!.visibility = View.INVISIBLE
        val eventImage: String = intent.getStringExtra("event_image")
        val eventId: String = intent.getStringExtra("event_id")
        picasso.load(ApiUtils.BASE_URL + eventImage).placeholder(R.drawable.logo).resize(90,90).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(upload_event_pic)
        upload_event_pic_btn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        }
        cancel_upload_event_btn.setOnClickListener {
            val i = Intent(this, MyEventActivity::class.java)
            startActivity(i)
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
        }
        confirm_upload_event_btn.setOnClickListener {
            if (fileImage != null) {
                //Log.e("path",filePart.toString())
                progressBar!!.visibility = View.VISIBLE
                val eventIdtranslated: RequestBody = RequestBody.create(MediaType.parse("text/plain"), eventId)
                uploadEventPhoto(eventIdtranslated, fileImage!!)
                finish()
            } else {
                toastMessage("Please select an image")
            }
        }
    }

    private fun uploadEventPhoto(eventId: RequestBody, image: MultipartBody.Part) {
        eventInterface.uploadEventPicture(eventId, image).enqueue(object : Callback<Reply> {
            override fun onFailure(call: Call<Reply>?, t: Throwable?) {
                Log.e(ContentValues.TAG, "Unable to get to API." + t?.message)
                if (t?.message == "unexpected end of steam") {
                    uploadEventPhoto(eventId, image)
                } else {
                    toastMessage("Check your internet connection")
                    progressBar!!.visibility = View.INVISIBLE
                    progressBar!!.progress = 0
                }
            }

            override fun onResponse(call: Call<Reply>?, response: Response<Reply>?) {
                if (response!!.isSuccessful) {
                    toastMessage("Event photo uploaded")
                } else {
                    toastMessage("Upload failed")
                }
            }
        })
    }

    private fun toastMessage(message: String) {
        val toast: Toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        val toastView: View = toast.view
        val toastMessage: TextView = toastView.findViewById(android.R.id.message)
        toastMessage.textSize = 20F
        toastMessage.setPadding(4, 4, 4, 4)
        toastMessage.setTextColor(Color.parseColor("#790e8b"))
        toastMessage.gravity = Gravity.CENTER
        toastView.setBackgroundColor(Color.YELLOW)
        toastView.setBackgroundResource(R.drawable.round_button1)
        toast.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri = data.data

            val realPath = ImageFilePath.getPath(this@UploadEventPictureActivity, data.data)
            val originalFile = File(realPath)

            Log.e("image path:", realPath)

            upload_event_pic.setImageURI(selectedImage) // Set thumbnail picture

            filePartImage = ProgressRequestBody(this, originalFile, this, selectedImage)
            fileImage = MultipartBody.Part.createFormData("image", originalFile.name, filePartImage)
        }
    }

    override fun onProgressUpdate(percentage: Long) {
        progressBar!!.progress = percentage.toInt()
        Log.e("percentage:", percentage.toString())
    }

    override fun onError() {
        toastMessage("Upload failed")
    }

    override fun onFinish() {
        progressBar!!.progress = 100
    }
}