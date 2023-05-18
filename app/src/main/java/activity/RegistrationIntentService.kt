package activity

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging


class RegistrationIntentService : IntentService(TAG) {

    companion object {
        private const val TAG = "RegIntentService"
        const val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
        const val FCM_TOKEN = "FCMToken"
    }

    private var token: String? = null

    override fun onHandleIntent(intent: Intent?) {
        // Make a call to Instance API

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isComplete) {
                    token = task.result
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // save token
            sharedPreferences.edit().putString(FCM_TOKEN, token).apply()
            // pass along this data
            sendRegistrationToServer(token)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to complete token refresh", e)
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply()
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply()
    }
}