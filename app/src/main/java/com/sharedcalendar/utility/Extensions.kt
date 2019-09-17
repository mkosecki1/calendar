package com.sharedcalendar.utility

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import java.text.SimpleDateFormat
import java.util.*

fun showMessage(context: Context, massage: String) =
    Toast.makeText(context, massage, Toast.LENGTH_LONG).show()

fun String.convertTimestamp(): Date {
    val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return parseFormat.parse(this)
}

fun Date.convertToday(): String {
    val resultFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return resultFormat.format(this)
}

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun Context.checkInternetConnection(): Boolean {
    val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
    return if (isConnected) {
        true
    } else {
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.no_internet_connection),
            Toast.LENGTH_LONG,
            R.style.myToastNoInternet
        ).show()
        false
    }
}

fun Context.startSound(sound: Int) = MediaPlayer.create(this, sound).start()

fun AppCompatActivity.setupActionBar(toolbar: Toolbar, titleId: Int) {
    this.setSupportActionBar(toolbar)
    supportActionBar?.let {
        title = getString(titleId)
        it.setDisplayHomeAsUpEnabled(false)
        it.setDisplayShowTitleEnabled(false)
    }
}