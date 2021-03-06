package com.example.cf.channelsd.Activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.Secure
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.cf.channelsd.Data.User
import com.example.cf.channelsd.Interfaces.LoginInterface
import com.example.cf.channelsd.R
import com.example.cf.channelsd.Utils.ApiUtils
import kotlinx.android.synthetic.main.activity_login.*
import org.parceler.Parcels
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var user: User? = null
    private var loginInterface: LoginInterface = ApiUtils.apiLogin

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val androidIdMain = Secure.getString(contentResolver, Secure.ANDROID_ID)
        val preferences: SharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
        //editor.clear()
        //editor.apply()
        val usernamePref: String = preferences.getString("username_pref", "")
        if (usernamePref.isEmpty()) {
            sign_in_btn.setOnClickListener() {
                if (checkTextFields() == 2) {
                    val username = input_username_user.text.toString()
                    val password = input_password_user.text.toString()
                    sendPost(username, password, androidIdMain)
                }
            }
        } else {
            user = User(
                    preferences.getString("session_key_pref", ""),
                    preferences.getString("username_pref", ""),
                    preferences.getString("email_pref", ""),
                    preferences.getString("userType_pref", ""),
                    preferences.getString("firstName_pref", ""),
                    preferences.getString("lastName_pref", ""),
                    preferences.getString("bio_pref", ""),
                    preferences.getString("profile_pic_pref", ""),
                    preferences.getString("profile_vid_pref", ""),
                    preferences.getString("profile_thumbnail_pref", "")
            )
            val i = Intent(this, DashboardActivity::class.java)
            i.putExtra("user", Parcels.wrap(user))
            startActivity(i)
            finish()
        }
        sign_up_now_btn.setOnClickListener() {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun popUpError(message: String, editText: EditText) {
        editText.error = message
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

    private fun editTextLength(editText: EditText): Int {
        return editText.text.toString().length
    }

    private fun checkTextFields(): Int {
        var checked = 0
        if (editTextLength(input_username_user) > 0) {
            checked++
        } else {
            popUpError("This field cannot be blank.", input_username_user)
        }
        if (editTextLength(input_password_user) > 0) {
            checked++
        } else {
            popUpError("This field cannot be blank.", input_password_user)
        }
        return checked
    }

    private fun sendPost(username: String, password: String, androidId: String) {
        loginInterface.sendUserInfo(username, password, androidId).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                Log.e(ContentValues.TAG, "Unable to get to API." + t?.message)
                if (t?.message == "unexpected end of stream") {
                    sendPost(username, password, androidId)
                } else {
                    toastMessage("Check your internet connection")
                }
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                if (response!!.isSuccessful) {
                    val userResponse = response.body()
                    Log.e("USER:", userResponse.toString())
                    val usernameR = userResponse?.username
                    val sessionKey = userResponse?.session_key
                    val email = userResponse?.email
                    val userType = userResponse?.userType
                    val firstName = userResponse?.firstName
                    val lastName = userResponse?.lastName
                    val bio = userResponse?.bio
                    val profilePic = userResponse?.profilePicture
                    val profileVid = userResponse?.profileVideo
                    val profileThumbNail = userResponse?.profileThumbNail
                    val preferences: SharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = preferences.edit()
                    editor.putString("session_key_pref", sessionKey.toString())
                    editor.putString("username_pref", usernameR.toString())
                    editor.putString("email_pref", email.toString())
                    editor.putString("userType_pref", userType.toString())
                    editor.putString("firstName_pref", firstName.toString())
                    editor.putString("lastName_pref", lastName.toString())
                    editor.putString("bio_pref", bio.toString())
                    editor.putString("profile_pic_pref", profilePic.toString())
                    editor.putString("profile_vid_pref", profileVid.toString())
                    editor.putString("profile_thumbnail_pref", profileThumbNail.toString())
                    editor.apply()
                    //Log.e(ContentValues.TAG, userType)
                    val i = Intent(this@MainActivity, DashboardActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    popUpError("Invalid username or password", input_username_user)
                }
            }
        })
    }
}

