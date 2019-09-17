package com.sharedcalendar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.utility.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        startActionBar()
        setupActionBar(toolbar_main_activity_id, R.string.main_activity_title)
        auth = FirebaseAuth.getInstance()
        isUserLogged()

        text_view_sign_in_id.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        button_login_id.setOnClickListener {
            startSound(R.raw.button)
            if (checkInternetConnection()) {
                progressbar_main_activity.visibility = View.VISIBLE
                loginUser()
            }
        }
    }

    private fun isUserLogged() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }
    }

    override fun onClick(view: View?) {
        if (view == text_view_sign_in_id) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun startActionBar() {
        supportActionBar?.also {
            title = getString(com.sharedcalendar.R.string.main_activity_title)
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayHomeAsUpEnabled(false)
        } ?: throw IllegalAccessException(getString(R.string.start_action_bar_error_text))
    }

//    private fun setupActionBar(toolbar: Toolbar) {
//        this.setSupportActionBar(toolbar)
//        supportActionBar?.let {
//            title = getString(R.string.main_activity_title)
//            it.setDisplayHomeAsUpEnabled(false)
//            it.setDisplayShowTitleEnabled(false)
//        }
//    }

    private fun loginUser() {
        val email = edit_text_login_email_id.text.toString().trim()
        val password = edit_text_login_password_id.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_email_text),
                Toast.LENGTH_LONG,
                R.style.myToastMail
            ).show()
            progressbar_main_activity.visibility = View.GONE
            return
        }
        if (TextUtils.isEmpty(password)) {
            showMessage(applicationContext, getString(R.string.enter_password_text))
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_password_text),
                Toast.LENGTH_LONG,
                R.style.myToastPassword
            ).show()
            progressbar_main_activity.visibility = View.GONE
            return
        }

        hideKeyboard()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    progressbar_main_activity.visibility = View.GONE
                    finish()
                } else {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.invalid_password_text),
                        Toast.LENGTH_LONG,
                        R.style.myToastInvalid
                    ).show()
                    progressbar_main_activity.visibility = View.GONE
                }
            }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }
}