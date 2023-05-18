package activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.autosherpas.autosherpa3.SplashActivity.MY_PREFS_NAME
import com.autosherpas.autosherpa3.SplashActivity.updateUrl
import com.autosherpas.autosherpa3.WyzConnectApp
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.R
import database.DBHelper
import database.DatabaseManager
import entity.CallInfo
import okhttp3.RequestBody
import receiver.NetworkReceiver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrointerface.APIClient
import retrointerface.AudioDataResponse
import retrointerface.Communicator
import service.AysncRecordSync
import service.LocationService
import service.RecordService
import service.UploadService
import util.CallHistory
import util.CommonSettings
import java.io.File

/**
 * Created By 1524 on 1/10/2020
 */
class CREActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.Calltobemade_TextView)
    var Callstomade: TextView? = null

    @BindView(R.id.servicebooked_TextView)
    var ServieBooked: TextView? = null

    @BindView(R.id.Conversationrate_TextView)
    var conversationRate: TextView? = null

    @BindView(R.id.Pendingcalls_TextView)
    var pendingCalls: TextView? = null

    @BindView(R.id.header_TextView)
    var dealerName: TextView? = null

    @BindView(R.id.Interaction_layout)
    var interactionLayout: RelativeLayout? = null

    @BindView(R.id.snackbarCoordinatorLayout)
    var snackbarCoordinatorLayout: CoordinatorLayout? = null

    @BindView(R.id.ic_toolbar)
    var ic_toolbar: Toolbar? = null

    @BindView(R.id.toolbar_version)
    var mVersionName: TextView? = null
    var settings: CommonSettings? = null
    var CRESummaryRef: DatabaseReference? = null
    var communicator: Communicator? = null
    var prefs: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var strRegistrationId: String? = null
    var strDisableUserStatus: String? = null
    var requestchecking = false
    var strdisableuser = "DisableUser"
    var uploadFirebaseFlag = false
    var progressDoalog: ProgressDialog? = null
    var dbhelper: DBHelper? = null
    var service: UploadService? = null
    var AudioStatusList: ArrayList<AudioDataResponse>? = null
    var commonSettings = ArrayList<CommonSettings?>()
    private var mNetworkReceiver: BroadcastReceiver? = null
    private var CRESummaryRef123: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cre)
        ButterKnife.bind(this)
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        editor = prefs.edit()
        settings = (applicationContext as WyzConnectApp).getSettings()
        /*
        if (checkPlayServices()) {
            Intent locintent = new Intent(this, LocationService.class);
            this.startService(locintent);
        }
*/commonSettings.add(settings)
        val extras = intent.extras
        if (extras != null) {
            val latestVersion = extras.getString("LatestVersion")
            val currentVersion = extras.getString("CurrentVersion")
            updateUrl = extras.getString("UpdateUrl")
            if (!latestVersion.equals(currentVersion, ignoreCase = true)) {
                showUpdateDialog()
            }
        }
        requestchecking = false
        id_cre = findViewById(R.id.id_cre)
        mNetworkReceiver = NetworkReceiver()
        registerNetworkBroadcastForNougat()
        initToolBar()
        //   getDisableUserStatus();
        communicator = Communicator()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        strRegistrationId = sharedPreferences.getString(FCM_TOKEN, "")
        Log.i("RegId", strRegistrationId!!)
        mAuthProgressDialog = MaterialDialog.Builder(this@CREActivity)
            .content(R.string.loading_text)
            .progress(true, 0)
            .canceledOnTouchOutside(false)
            .show()
        dealerName.setText(settings.getUserId())
        mAuthProgressDialog!!.dismiss()
        CRESummaryRef123 =
            FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.CREHISTORY_URL)
        CRESummaryRef123!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount != 0L) {
                    for (snap in dataSnapshot.children) {
                        Log.e(snap.key, snap.childrenCount.toString() + "")
                        if (snap.childrenCount > 10) {
                            Log.e(snap.key, snap.childrenCount.toString() + "")
                        } else {
                            snap.ref.removeValue()
                            Log.e(snap.key, snap.childrenCount.toString() + "")
                        }
                    }
                }
                Log.e(dataSnapshot.key, dataSnapshot.childrenCount.toString() + "")
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        CRESummaryRef = FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.CRE_URL)
        CRESummaryRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    interactionLayout!!.visibility = View.VISIBLE
                    for (snapshot in dataSnapshot.children) {
                        val post = snapshot.getValue(CallInfo::class.java)
                        Callstomade!!.text = post!!.totalAssignedCalls
                        ServieBooked!!.text = post.serviceBooked
                        conversationRate!!.text = post.getconversionRate()
                        pendingCalls!!.text = post.pendingCalls
                        mAuthProgressDialog!!.dismiss()
                    }
                } else {
                    interactionLayout!!.visibility = View.GONE
                    mAuthProgressDialog!!.dismiss()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@CREActivity, databaseError.message, Toast.LENGTH_SHORT).show()
                Log.d("Error", databaseError.message)
            }
        })
        startService(Intent(this, AysncRecordSync::class.java))
    }

    val disableUserStatus: Unit
        get() {
            val ref = FirebaseDatabase.getInstance().getReferenceFromUrl(
                settings!!.getSYNC_SOURCE() + settings.getDealerId()
                    .toString() + "/users/" + settings.getUserId()
            )
            ref.child(strdisableuser).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Snapshot Value :" + snapshot.value)
                    if (snapshot.value == null) {
                        Log.d(TAG, "No Tree Available")
                    } else {
                        strDisableUserStatus = snapshot.value.toString()
                        Log.i(
                            TAG,
                            "Disable Status :$strDisableUserStatus"
                        )
                        if (strDisableUserStatus.equals(
                                "True",
                                ignoreCase = true
                            ) || strDisableUserStatus.equals("true", ignoreCase = true)
                        ) {
                            settings.setDisableUser(true)
                            Toast.makeText(
                                applicationContext,
                                R.string.userdisabled,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            settings.setDisableUser(false)
                        }
                    }
                }

                override fun onCancelled(firebaseError: DatabaseError) {
                    Log.e(TAG, "Firebase Error" + firebaseError.message)
                }
            })
        }

    override fun onConnected(bundle: Bundle?) {}
    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    private fun checkPlayServices(): Boolean {
        val googleApi = GoogleApiAvailability.getInstance()
        val resultCode = googleApi
            .isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApi.isUserResolvableError(resultCode)) {
                googleApi.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)!!
                    .show()
            } else {
                Toast.makeText(
                    applicationContext,
                    R.string.nogoogleplay_text, Toast.LENGTH_LONG
                )
                    .show()
            }
            return false
        }
        return true
    }

    private fun showUpdateDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("New version available")
            .setMessage("Please, update app to new version to continue.")
            .setPositiveButton(
                "Update"
            ) { dialog1: DialogInterface?, which: Int ->
                val PACKAGE_NAME: String
                PACKAGE_NAME = applicationContext.packageName
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:$PACKAGE_NAME")
                startActivity(intent)
                redirectStore(updateUrl)
            }.setNegativeButton(
                "No, thanks"
            ) { dialog12: DialogInterface?, which: Int ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask()
                } else {
                    finishAffinity()
                }
            }.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun redirectStore(updateUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun initToolBar() {
        setSupportActionBar(ic_toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(true)
        }
        versionCode
    }

    val versionCode: Unit
        get() {
            var pInfo: PackageInfo? = null
            try {
                pInfo = packageManager.getPackageInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            val version = pInfo!!.versionName
            mVersionName!!.text = "CRE V $version"
        }

    private fun registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                mNetworkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        settings.isCheckForeground = true
        val networksnackbar: Snackbar =
            Snackbar.make(ic_toolbar, R.string.nointernet_text, Snackbar.LENGTH_INDEFINITE)
        networksnackbar.setAction(R.string.settings_text) { v -> startActivity(Intent(Settings.ACTION_SETTINGS)) }
        networksnackbar.setActionTextColor(Color.RED)
        val sbView = networksnackbar.view
        val textView = sbView.findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.YELLOW)
        networksnackbar.show()
        if (!CommonSettings.instance.haveNetworkConnection()) {
            networksnackbar.show()
            Log.d("CREActivity", "No network displaying snackbar!")
        }
        if (CommonSettings.instance.haveNetworkConnection()) {
            networksnackbar.dismiss()
            Log.d("CREActivity", " Enabled Network dismissing snackbar!")
        }
    }

    override fun onStart() {
        super.onStart()
    }

    fun settingsRequest() {
        val mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build()
        mGoogleApiClient.connect()
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = LocationService.UPDATE_INTERVAL.toLong()
        locationRequest.fastestInterval = LocationService.FATEST_INTERVAL.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true) //this is the key ingredient
        val result =
            LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build())
        result.setResultCallback { result1: LocationSettingsResult ->
            val status = result1.status
            val state = result1.locationSettingsStates
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {}
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> if (!requestchecking) {


                    try {
                        status.startResolutionForResult(
                            this@CREActivity,
                            REQUEST_CHECK_SETTINGS
                        )
                        requestchecking = true
                    } catch (e: SendIntentException) {
                        // Ignore the error.
                    }
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
            }
        }
    }

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cre_menu, menu)
        for (i in 0 until menu.size()) {
            val drawable = menu.getItem(i).icon
            if (drawable != null) {
                drawable.mutate()
                drawable.setColorFilter(
                    resources.getColor(R.color.color_background),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.creupload -> {
                @SuppressLint("HandlerLeak") val handle: Handler = object : Handler() {
                    override fun handleMessage(msg: Message) {
                        super.handleMessage(msg)
                        progressDoalog!!.incrementProgressBy(1)
                    }
                }
                dbhelper = DBHelper(this@CREActivity)
                DatabaseManager.initializeInstance(dbhelper)
                val files: ArrayList<String> = dbhelper.getNotSyncedFileAll()
                if (!files.isEmpty()) {
                    progressDoalog = ProgressDialog(this@CREActivity)
                    progressDoalog!!.max = files.size
                    progressDoalog!!.setMessage(getString(R.string.uploading_text))
                    progressDoalog!!.setTitle("Please wait.")
                    progressDoalog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                    progressDoalog!!.show()
                    Thread {
                        try {
                            while (progressDoalog!!.progress < progressDoalog!!.max) {
                                Thread.sleep(200)
                                if (CommonSettings.instance.haveNetworkConnection()) {
                                    try {
                                        if (!files.isEmpty()) {
                                            for (filename in files) {
                                                if (settings.deviceManufacturer
                                                        .equalsIgnoreCase("Intex")
                                                ) {
                                                    uploadIntexFileRecUsingRetrofit(filename)
                                                } else {
                                                    UploadFileUsingRetroFit(filename)
                                                }
                                            }
                                            println("syncRecord : " + files.size)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    handle.sendMessage(handle.obtainMessage())
                                    progressDoalog!!.dismiss()
                                    Toast.makeText(
                                        this@CREActivity,
                                        "No Internet Connection.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                handle.sendMessage(handle.obtainMessage())
                                if (progressDoalog!!.progress == progressDoalog!!.max) {
                                    progressDoalog!!.dismiss()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.start()
                } else {
                    ShowUploadSuccessDialog()
                }
                Log.d("Uploading Data", "DataUploading")
                true
            }
            R.id.crelogout -> {
                val builder1 = AlertDialog.Builder(this@CREActivity)
                builder1.setMessage(" you want to logout...")
                builder1.setTitle("Are you sure!")
                builder1.setCancelable(true)
                builder1.setNegativeButton(
                    "Cancel"
                ) { dialog: DialogInterface, i: Int -> dialog.cancel() }
                builder1.setPositiveButton(
                    "Yes"
                ) { dialog: DialogInterface, id: Int ->
                    dialog.cancel()
                    editor!!.remove("DealerUrl")
                    editor!!.clear()
                    editor!!.commit()
                    FirebaseAuth.getInstance().signOut()
                    settings.setIsAuthenticated(false)
                    val intent =
                        Intent(applicationContext, SplashActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finishAffinity()
                }
                val alert11 = builder1.create()
                alert11.show()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun ShowUploadSuccessDialog() {

        // Build an AlertDialog
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(R.layout.my_success_dialog, null)

        // Specify alert dialog is not cancelable/not ignorable
        builder.setCancelable(false)

        // Set the custom layout as alert dialog view
        builder.setView(dialogView)

        // Get the custom alert dialog view widgets reference
        val btn_positive = dialogView.findViewById<View>(R.id.buttonOk) as Button

        // Create the alert dialog
        val dialog = builder.create()

        // Set positive/yes button click listener
        btn_positive.setOnClickListener { v: View? ->
            // Dismiss the alert dialog
            dialog.cancel()
        }

        // Display the custom alert dialog on interface
        dialog.show()
    }

    fun uploadIntexFileRecUsingRetrofit(uniqueId: String?): Boolean {
        val dealerUrl = prefs!!.getString("DealerUrl", "")
        service = Retrofit.Builder().baseUrl(dealerUrl).build().create(UploadService::class.java)
        dbhelper = DBHelper(this)
        var name: RequestBody
        val custNumber: String = settings.userMobileNo.replaceAll("\\s+", "")
        val DEFAULT_STORAGE_LOCATION = "/sdcard/Auto Call Record/"
        AudioStatusList = ArrayList<AudioDataResponse>()
        val files = getListFiles(File(DEFAULT_STORAGE_LOCATION), custNumber, custNumber)
        if (files.size > 0 && !files.isEmpty()) {
            for (file in files) {
                val reqFile =
                    RequestBody.create(parse.parse("audio*//**//**//**//**//**//**//**//*"), file)
                val body: Part = createFormData.createFormData(
                    "audio",
                    settings.getDealerId() + "/" + file.name,
                    reqFile
                )
                if (uniqueId == null) {
                    name = RequestBody.create(parse.parse("text/plain"), "0")
                } else {
                    name = RequestBody.create(parse.parse("text/plain"), uniqueId)
                    val req: Call<ArrayList<AudioDataResponse>> = service.postImage(body, name)
                    req.enqueue(object : Callback<ArrayList<AudioDataResponse>?> {
                        override fun onResponse(
                            call: Call<ArrayList<AudioDataResponse>?>,
                            response: Response<ArrayList<AudioDataResponse>?>
                        ) {
                            try {
                                println("Response :" + response.message())
                                AudioStatusList = response.body()
                                assert(response.body() != null)
                                val status: String = response.body()!![0].getStatus()
                                val audioFile: String = response.body()!![0].getFilename()
                                uploadFirebaseFlag = true
                                try {
                                    if (AudioStatusList!!.size > 0) {
                                        dbhelper = DBHelper(applicationContext)
                                        for (data in AudioStatusList) {
                                            dbhelper.updateSyncStatus(audioFile, status)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onFailure(
                            call: Call<ArrayList<AudioDataResponse>?>,
                            t: Throwable
                        ) {
                            t.printStackTrace()
                            println("Error Response :" + t.message)
                            uploadFirebaseFlag = false
                        }
                    })
                    println("uploadFirebaseFlag : $uploadFirebaseFlag")
                }
            }
        }
        return uploadFirebaseFlag
    }

    private fun getListFiles(
        parentDir: File,
        custNumber: String,
        DirectoryName: String
    ): List<File> {
        var DirectoryName = DirectoryName
        val inFiles = ArrayList<File>()
        val files = parentDir.listFiles()
        if (files.size > 0) {
            for (file in files) {
                if (!file.name.isEmpty()) {
                    val fileName = file.name.replace("\\s+".toRegex(), "")
                    if (fileName.contains(custNumber) || file.absolutePath.contains(DirectoryName)) {
                        if (file.isDirectory) {
                            DirectoryName = file.absolutePath
                            inFiles.addAll(getListFiles(file, custNumber, DirectoryName))
                        } else {
                            if (file.name.endsWith(".mp3")) {
                                val oldFile = File(parentDir, file.name)
                                val latestname = File(parentDir, settings.fileNameWithUniqueId)
                                val success = oldFile.renameTo(latestname)
                                if (success) println("file is renamed..")
                                inFiles.add(latestname)
                            }
                        }
                    }
                }
            }
        }
        return inFiles
    }

    fun UploadFileUsingRetroFit(uniqueId: String?): Boolean {
        val dealerUrl = prefs!!.getString("DealerUrl", "")
        val service: UploadService =
            APIClient.getClient(dealerUrl).create(UploadService::class.java)
        dbhelper = DBHelper(this)
        AudioStatusList = ArrayList<AudioDataResponse>()
        val name: RequestBody
        /*RequestBody dealercode;*/
        val file = File(RecordService.DEFAULT_STORAGE_LOCATION + "/" + uniqueId)
        try {
            if (file.exists()) {
                val reqFile = RequestBody.create(parse.parse("audio/*"), file)
                val body: Part = createFormData.createFormData("audio", file.name, reqFile)
                if (uniqueId == null) {
                    name = RequestBody.create(parse.parse("text/plain"), "0")
                } else {
                    name = RequestBody.create(parse.parse("text/plain"), uniqueId)
                    try {
/*
                        dealercode = RequestBody.create( MediaType.parse( "text/plain" ), settings.getDealerId() );
*/
                        val req: Call<ArrayList<AudioDataResponse>> = service.postImage(body, name)
                        req.enqueue(object : Callback<ArrayList<AudioDataResponse>?> {
                            override fun onResponse(
                                call: Call<ArrayList<AudioDataResponse>?>,
                                response: Response<ArrayList<AudioDataResponse>?>
                            ) {
                                try {
                                    println("Response :" + response.message())
                                    AudioStatusList = response.body()
                                    assert(response.body() != null)
                                    val yourFilePath =
                                        RecordService.DEFAULT_STORAGE_LOCATION + "/" + uniqueId
                                    val file = File(yourFilePath)
                                    val result = file.delete()
                                    sendBroadcast(
                                        Intent(
                                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                            Uri.fromFile(file)
                                        )
                                    )
                                    assert(response.body() != null)
                                    val status: String = response.body()!![0].getStatus()
                                    val audioFile: String = response.body()!![0].getFilename()
                                    uploadFirebaseFlag = true
                                    try {
                                        if (AudioStatusList!!.size > 0) {
                                            dbhelper = DBHelper(applicationContext)
                                            for (data in AudioStatusList) {
                                                dbhelper.updateSyncStatus(audioFile, status)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onFailure(
                                call: Call<ArrayList<AudioDataResponse>?>,
                                t: Throwable
                            ) {
                                t.printStackTrace()
                                println("Error Response :" + t.message)
                                uploadFirebaseFlag = false
                            }
                        })
                        println("uploadFirebaseFlag : $uploadFirebaseFlag")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (fe: Exception) {
            fe.printStackTrace()
        }
        return uploadFirebaseFlag
    }

    override fun onPause() {
        super.onPause()
        settings.isCheckForeground = false
    }

    override fun onStop() {
        super.onStop()
        settings.isCheckForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterNetworkChanges()
        settings.isCheckForeground = false
    }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 1000
        var mAuthProgressDialog: MaterialDialog? = null
        protected const val REQUEST_CHECK_SETTINGS = 0x1
        const val FCM_TOKEN = "FCMToken"
        const val TAG = "CREACT"
        var id_cre: LinearLayoutCompat? = null
    }
}