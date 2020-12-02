package com.example.register

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private var firstTimeUser = true

    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        buttonClicks()
    }

    private fun buttonClicks() {
        btn_login.setOnClickListener{
            firstTimeUser = false
            createOrLoginUser()
        }
btn_register.setOnClickListener(){
    firstTimeUser = true
    createOrLoginUser ()
}
        iv_profileImage.setOnClickListener{
            selectImage()
        }
    }
    private fun createOrLoginUser(){
        var email: et_emailLogin.text.toString()
        var password: et_passwordLogin.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()){
            GlobalScope.launch(Dispatchers.IO) {
                try{
                    if(firstTimeUser){
                        auth.createUserWithEmailAndPassword(email, password).await()
                        auth.currentUser.let {
                            val update = UserProfileChangeRequest.Builder()
                                .setPhotoUri(fileUri)
                                .build()
                            it?.updateProfile(update)
                        }?.await()
                    }else{
                        auth.signInWithEmailAndPassword(email, password).await()
                    }
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, "You are loggedIN", Toast.LENGTH_SHORT).show()

                        val i = Intent(this@MainActivity, UserActivity::class.java)
                        startActivity(i)
                        finish()
                    }

                } catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    private fun checkIfUserIsLoggedIn(){
        if(auth.currentUser != null){
            val i = Intent(this@MainActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        checkIfUserIsLoggedIn()
    }
    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            Activity.RESULT_OK->{
                fileUri = data?.data
                iv_profileImage.setImageURI(fileUri)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}





