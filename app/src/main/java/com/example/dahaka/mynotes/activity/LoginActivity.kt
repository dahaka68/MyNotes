package com.example.dahaka.mynotes.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.view.View
import com.crashlytics.android.Crashlytics
import com.example.dahaka.mynotes.Constants.EMAIL
import com.example.dahaka.mynotes.Constants.PROFILE
import com.example.dahaka.mynotes.Constants.SKIP
import com.example.dahaka.mynotes.R
import com.example.dahaka.mynotes.model.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.optimizely.Optimizely
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userEmail: String
    private lateinit var userPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Optimizely.startOptimizelyWithAPIToken(getString(R.string.com_optimizely_api_key),application)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_login)
        e_mail.setText(getSharedPreferences(EMAIL, Context.MODE_PRIVATE).getString(EMAIL, null))
        sign_in.setOnClickListener {
            signIn()
        }
        skip.setOnClickListener {
            startActivity<MainActivity>(SKIP to "skip")
        }
    }

    private fun signUp() {
        progress_bar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: userEmail
                    val profileDatabaseRef = FirebaseDatabase.getInstance().getReference(PROFILE)
                    val profile = Profile(userEmail, userPassword, uid)
                    profileDatabaseRef.child(uid).setValue(profile)
                    progress_bar.visibility = View.GONE
                    toast(R.string.success)
                    getSharedPreferences(EMAIL, Context.MODE_PRIVATE).edit().putString(EMAIL, userEmail).apply()
                    startMainActivity()
                }
                else -> {
                    progress_bar.visibility = View.GONE
                    val message = task.exception?.message ?: getString(R.string.error)
                    toast(message)
                }
            }
        }
    }

    private fun signIn() {
        userEmail = e_mail.text.toString().trim { it <= ' ' }
        userPassword = password.text.toString().trim { it <= ' ' }
        when {
            isEmpty(userEmail) -> toast(R.string.enter_email)
            isEmpty(userPassword) -> toast(R.string.enter_password)
            userPassword.length < 6 -> toast(R.string.short_password)
            else -> {
                progress_bar.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(this) { task ->
                            progress_bar.visibility = View.GONE
                            when {
                                !task.isSuccessful -> signUp()
                                else -> {
                                    getSharedPreferences(EMAIL, Context.MODE_PRIVATE).edit().putString(EMAIL, userEmail).apply()
                                    startMainActivity()
                                }
                            }
                        }
            }
        }
    }

    private fun startMainActivity() {
        startActivity<MainActivity>()
        finish()
    }
}
