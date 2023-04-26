package activity

import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.RequiresApi
import android.os.Build
import android.os.Bundle
import com.demo.autosherpa3.R
import android.content.Intent
import android.net.Uri
import util.CommonSettings

class CallTriggerActivity : AppCompatActivity() {
    var phoneNumber: String? = null
    private val settings: CommonSettings? = null


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_trigger)
        val settings=(application as WyzConnectApp).settings
        val phoneNumber=settings.getUserPhoneNumber()
        val Intent =intent(Intent.ACTION_CALL)
        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data=Uri.parse("tel:$phoneNumber")

        val extras = intent.extras
        if (extras != null) {
            phoneNumber = extras.getString("phoneNumber")
        }
        val intent = Intent(Intent.ACTION_CALL)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // intent.setData( Uri.parse("tel:" + receivedphonenumber));
        intent.data = Uri.parse("tel:$phoneNumber")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
        finishAndRemoveTask()
    }
}