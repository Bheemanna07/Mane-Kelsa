package com.kvsn.builds.cap1

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class CheckInternetConnection(private val ctx: Context) {

    fun checkConnection() {
        if (!isInternetConnected()) {
            val builder = AlertDialog.Builder(ctx)
            builder.setTitle("No Internet")
                .setMessage(R.string.noconnection)
                .setCancelable(false)
                .setPositiveButton("Connect Now") { dialog, _ ->
                    if (isInternetConnected()) {
                        dialog.dismiss()
                    } else {
                        val dialogIntent = Intent(Settings.ACTION_SETTINGS)
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(dialogIntent)
                    }
                }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun isInternetConnected(): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
}
