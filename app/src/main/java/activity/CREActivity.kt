package activity


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
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
import com.afollestad.materialdialogs.MaterialDialog
import com.demo.autosherpa3.R
import com.demo.autosherpa3.SplashActivity
import com.demo.autosherpa3.WyzConnectApp
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import util.CommonSettings

//extended Appcompatactivity and implemented GoogleApiClient
class CREActivity : AppCompatActivity() , GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    @BindView(R.id.Calltobemade_TextView)
    lateinit var Callstomade: TextView
    @BindView(R.id.servicebooked_TextView)
    lateinit var ServieBooked: TextView
    @BindView(R.id.Conversationrate_TextView)
    lateinit var conversationRate: TextView
    @BindView(R.id.Pendingcalls_TextView)
    lateinit var pendingCalls: TextView
    @BindView(R.id.header_TextView)
    lateinit var dealerName: TextView
    @BindView(R.id.Interaction_layout)
    lateinit var interactionLayout: RelativeLayout
    @BindView(R.id.snackbarCoordinatorLayout)
    lateinit var snackbarCoordinatorLayout: CoordinatorLayout
    @BindView(R.id.ic_toolbar)
    lateinit var ic_toolbar: Toolbar
    @BindView(R.id.toolbar_version)
    lateinit var mVersionName: TextView
    lateinit var settings: CommonSettings
    lateinit var CRESummaryRef: DatabaseReference
    lateinit var communicator: Communicator
    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 1000
        var mAuthProgressDialog: MaterialDialog? = null
        protected const val REQUEST_CHECK_SETTINGS = 0x1
        const val FCM_TOKEN = "FCMToken"
        const val TAG = "CREACT"
    }

    var strRegistrationId: String? = null
    var strDisableUserStatus: String? = null
    var requestchecking = false
    var strdisableuser = "DisableUser"
    var uploadFirebaseFlag = false
    var progressDoalog: ProgressDialog? = null
    var dbhelper: DBHelper? = null
    var service: UploadService? = null
    var id_cre: LinearLayoutCompat? = null
    val AudioStatusList = ArrayList<AudioDataResponse>()
    val commonSettings = ArrayList<CommonSettings>()
    private var mNetworkReceiver: BroadcastReceiver? = null
    private var CRESummaryRef123: DatabaseReference? = nul


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cre)
        ButterKnife.bind(this)
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        editor = prefs.edit()
        settings = (applicationContext as WyzConnectApp).settings

        commonSettings.add(settings)
        val extras = intent.extras
        if (extras != null) {
            val latestVersion = extras.getString("LatestVersion")
            val currentVersion = extras.getString("CurrentVersion")
            updateUrl = extras.getString("UpdateUrl")

            if (latestVersion != currentVersion) {
                showUpdateDialog()
            }
        }

        requestchecking = false
        id_cre = findViewById(R.id.id_cre)
        mNetworkReceiver = NetworkReceiver()
        registerNetworkBroadcastForNougat()
        initToolBar()
        // getDisableUserStatus()
        communicator = Communicator()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        strRegistrationId = sharedPreferences.getString(FCM_TOKEN, "") ?: ""
        Log.i("RegId", strRegistrationId)

        mAuthProgressDialog = MaterialDialog.Builder(this)
            .content(R.string.loading_text)
            .progress(true, 0)
            .canceledOnTouchOutside(false)
            .show()
        dealerName.text = settings.getUserId()
        mAuthProgressDialog.dismiss()
        CRESummaryRef123 = FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.CREHISTORY_URL)

        CRESummaryRef123.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount != 0L) {
                    for (snap in dataSnapshot.children) {
                        Log.e(snap.key!!, snap.childrenCount.toString() + "")
                        if (snap.childrenCount > 10) {
                            Log.e(snap.key!!, snap.childrenCount.toString() + "")
                        } else {
                            snap.ref.removeValue()
                            Log.e(snap.key!!, snap.childrenCount.toString() + "")
                        }
                    }
                }
                Log.e(dataSnapshot.key!!, dataSnapshot.childrenCount.toString() + "")
            }
    })
                CRESummaryRef = FirebaseDatabase.getInstance().getReferenceFromUrl(CallHistory.CRE_URL)

                CRESummaryRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            interactionLayout.visibility = View.VISIBLE
                            for (snapshot in dataSnapshot.children) {
                                val post = snapshot.getValue(CallInfo::class.java)
                                Callstomade.text = post?.totalAssignedCalls
                                ServieBooked.text = post?.serviceBooked
                                conversationRate.text = post?.conversionRate
                                pendingCalls.text = post?.pendingCalls
                                mAuthProgressDialog.dismiss()
                            }
                        } else {
                            interactionLayout.visibility = View.GONE
                            mAuthProgressDialog.dismiss()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@CREActivity,
                            databaseError.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("Error", databaseError.message.toString())
                    }
                })
            startService(Intent(this, AysncRecordSync::class.java))

    }
            fun getDisableUserStatus() {
                val ref = FirebaseDatabase.getInstance().getReferenceFromUrl(settings.getSYNC_SOURCE() + settings.getDealerId() + "/users/" + settings.getUserId())
                ref.child(strdisableuser).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "Snapshot Value : ${snapshot.value}")
                        if (snapshot.value == null) {
                            Log.d(TAG, "No Tree Available")
                        } else {
                            strDisableUserStatus = snapshot.value.toString()
                            Log.i(TAG, "Disable Status :$strDisableUserStatus")
                            if (strDisableUserStatus.equals("True", ignoreCase = true)) {
                                settings.setDisableUser(true)
                                Toast.makeText(applicationContext, R.string.userdisabled, Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                settings.setDisableUser(false)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Error", databaseError.getMessage().toString())
                        Toast.makeText(applicationContext, error.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }

            fun getDisableUserStatus() {
                val ref = FirebaseDatabase.getInstance().getReferenceFromUrl(settings.getSYNC_SOURCE() + settings.getDealerId() + "/users/" + settings.getUserId())
                ref.child(strdisableuser).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d(TAG, "Snapshot Value : ${snapshot.value}")
                        if (snapshot.value == null) {
                            Log.d(TAG, "No Tree Available")
                        } else {
                            strDisableUserStatus = snapshot.value.toString()
                            Log.i(TAG, "Disable Status :$strDisableUserStatus")
                            if (strDisableUserStatus.equals("True", ignoreCase = true)) {
                                settings.setDisableUser(true)
                                Toast.makeText(applicationContext, R.string.userdisabled, Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                settings.setDisableUser(false)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Error :${error.message}")
                        Toast.makeText(applicationContext, error.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }
                    private fun checkPlayServices(): Boolean {
                val googleApi = GoogleApiAvailability.getInstance()
                val resultCode = googleApi.isGooglePlayServicesAvailable(this)
                if (resultCode != ConnectionResult.SUCCESS) {
                    if (googleApi.isUserResolvableError(resultCode)) {
                        googleApi.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)?.show()
                    } else {
                        Toast.makeText(applicationContext,
                            R.string.nogoogleplay_text, Toast.LENGTH_LONG)
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
                    .setPositiveButton("Update") { dialog, which ->
                        var PACKAGE_NAME: String
                        PACKAGE_NAME = applicationContext.packageName
                        val intent = Intent(Intent.ACTION_DELETE)
                        intent.data = Uri.parse("package:$PACKAGE_NAME")
                        startActivity(intent)
                        redirectStore(updateUrl)
                    }
                    .setNegativeButton("No, thanks") { dialog, which ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            finishAndRemoveTask()
                        } else {
                            finishAffinity()
                        }
                    }
                    .create()
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
                supportActionBar?.setDisplayShowTitleEnabled(true)
                getVersionCode()
            }
            fun getVersionCode() {
                var pInfo: PackageInfo? = null
                try {
                    pInfo = packageManager.getPackageInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
                val version = pInfo?.versionName
                mVersionName.text = "CRE V $version"
            }

                    private fun registerNetworkBroadcastForNougat() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    registerReceiver(mNetworkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
                }
            }

    override fun onResume() {
        super.onResume()
        settings.checkForeground = true
        val networksnackbar: Snackbar =
            Snackbar.make(ic_toolbar, R.string.nointernet_text, Snackbar.LENGTH_INDEFINITE)
        networksnackbar.setAction(R.string.settings_text) { v -> startActivity(Intent(Settings.ACTION_SETTINGS)) }
        networksnackbar.setActionTextColor(Color.RED)
        val sbView = networksnackbar.view
        val textView = sbView.findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.YELLOW)
        networksnackbar.show()
        if (!CommonSettings.getInstance().haveNetworkConnection()) {
            networksnackbar.show()
            Log.d("CREActivity", "No network displaying snackbar!")
        }
        if (CommonSettings.getInstance().haveNetworkConnection()) {
            // if (networksnackbar.isShown()) {
            networksnackbar.dismiss()
            Log.d("CREActivity", " Enabled Network dismissing snackbar!")
            //  }
        }
    }
                    override fun onStart() {
                super.onStart()
// settingsRequest()
            }

                    private fun settingsRequest() {
                val mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
                mGoogleApiClient.connect()
                val locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = LocationService.UPDATE_INTERVAL
                locationRequest.fastestInterval = LocationService.FATEST_INTERVAL
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                builder.setAlwaysShow(true) // this is the key ingredient
                val result: PendingResult<LocationSettingsResult> =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build())
                result.setResultCallback { result1: LocationSettingsResult ->
                    val status: Status = result1.status
                    val state: LocationSettingsStates = result1.locationSettingsStates
                    when (status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS -> {
// All location settings are satisfied. The client can initialize location
// requests here.
                        }
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            if (!requestchecking) {
// Location settings are not satisfied. But could be fixed by showing the user
// a dialog.
                                try {
// Show the dialog by calling startResolutionForResult(),
// and check the result in onActivityResult().
                                    status.startResolutionForResult(this@CREActivity, REQUEST_CHECK_SETTINGS)
                                    requestchecking = true
                                } catch (e: IntentSender.SendIntentException) {
// Ignore the error.
                                }
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
// Location settings are not satisfied. However, we have no way to fix the
// settings so we won't show the dialog.
                        }
                    })
                }
    protected fun unregisterNetworkChanges(){
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
            }


                    return super.onCreateOptionsMenu(menu)
                }
                    override fun onOptionsItemSelected(item: MenuItem): Boolean {
// Handle item selection
                when (item.itemId) {
                    R.id.creupload -> {
                        val handle: Handler = object : Handler() {
                            override fun handleMessage(msg: Message) {
                                super.handleMessage(msg)
                                progressDoalog.incrementProgressBy(1)
                            }
                        }
                        @Override
                        public boolean onOptionsItemSelected(MenuItem item) {
                            // Handle item selection
                            switch (item.getItemId()) {
                                case R.id.creupload:
                                @SuppressLint("HandlerLeak") final Handler handle = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        progressDoalog.incrementProgressBy(1);
                                    }
                                };

                                dbhelper = new DBHelper(CREActivity.this);
                                DatabaseManager.initializeInstance(dbhelper);
                                final ArrayList<String> files = dbhelper.getNotSyncedFileAll();
                                if (!files.isEmpty()) {
                                    progressDoalog = new ProgressDialog(CREActivity.this);
                                    progressDoalog.setMax(files.size());
                                    progressDoalog.setMessage(getString(R.string.uploading_text));
                                    progressDoalog.setTitle("Please wait.");
                                    progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDoalog.show();
                                    new Thread(() -> {
                                        try {
                                            while (progressDoalog.getProgress() < progressDoalog.getMax()) {
                                                Thread.sleep(200);
                                                if (CommonSettings.getInstance().haveNetworkConnection()) {
                                                    try {
                                                        if (!files.isEmpty()) {
                                                            for (String filename : files) {

                                                                if (settings.getDeviceManufacturer().equalsIgnoreCase("Intex")) {
                                                                    uploadIntexFileRecUsingRetrofit(filename);
                                                                } else {
                                                                    UploadFileUsingRetroFit(filename);
                                                                }

                                                            }
                                                            System.out.println("syncRecord : " + files.size());
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }


                                                } else {
                                                    handle.sendMessage(handle.obtainMessage());
                                                    progressDoalog.dismiss();
                                                    Toast.makeText(CREActivity.this, "No Internet Connection.", Toast.LENGTH_LONG).show();
                                                }
                                                handle.sendMessage(handle.obtainMessage());

                                                if (progressDoalog.getProgress() == progressDoalog.getMax()) {
                                                    progressDoalog.dismiss();
                                                }
                                            }
                                            override fun onOptionsItemSelected(item: MenuItem): Boolean {
// Handle item selection
                                                when (item.itemId) {
                                                    R.id.creupload -> {
                                                        val handle: Handler = object : Handler() {
                                                            override fun handleMessage(msg: Message) {
                                                                super.handleMessage(msg)
                                                                progressDoalog.incrementProgressBy(1)
                                                            }
                                                        }
                                                        dbhelper = DBHelper(this@CREActivity)
                                                        DatabaseManager.initializeInstance(dbhelper)
                                                        val files = dbhelper.getNotSyncedFileAll()
                                                        if (files.isNotEmpty()) {
                                                            progressDoalog = ProgressDialog(this@CREActivity)
                                                            progressDoalog.max = files.size
                                                            progressDoalog.setMessage(getString(R.string.uploading_text))
                                                            progressDoalog.setTitle("Please wait.")
                                                            progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                                                            progressDoalog.show()
                                                            Thread {
                                                                try {
                                                                    while (progressDoalog.progress < progressDoalog.max) {
                                                                        Thread.sleep(200)
                                                                        if (CommonSettings.getInstance().haveNetworkConnection()) {
                                                                            try {
                                                                                if (files.isNotEmpty()) {
                                                                                    for (filename in files) {
                                                                                        if (settings.deviceManufacturer.equals("Intex", ignoreCase = true)) {
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
                                                                            progressDoalog.dismiss()
                                                                            Toast.makeText(this@CREActivity, "No Internet Connection.", Toast.LENGTH_LONG).show()
                                                                        }
                                                                        handle.sendMessage(handle.obtainMessage())
                                                                        if (progressDoalog.progress == progressDoalog.max) {
                                                                            progressDoalog.dismiss()
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
                                                        return true
                                                    }
                                                    R.id.crelogout -> {
                                                        val builder1 = AlertDialog.Builder(this@CREActivity)
                                                        builder1.setMessage(" you want to logout...")
                                                        builder1.setTitle("Are you sure!")
                                                        builder1.setCancelable(true)
                                                        builder1.setNegativeButton("Cancel") { dialog, i -> dialog.cancel() }
                                                        builder1.setPositiveButton(
                                                            "Yes"
                                                        ) { dialog, id ->
                                                            dialog.cancel()
                                                            editor.remove("DealerUrl")
                                                            editor.clear()
                                                            editor.commit()
                                                            FirebaseAuth.getInstance().signOut()
                                                            settings.isAuthenticated = false
                                                            val intent = Intent(applicationContext, SplashActivity::class.java)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                            startActivity(intent)
                                                            finishAffinity()
                                                        }
                                                        val alert11 = builder1.create()
                                                        alert11.show()
                                                    }
                                                    else -> return super.onOptionsItemSelected(item)
                                                }
                                                return true
                                            }

                                            private fun showUploadSuccessDialog() {
                                                val builder = AlertDialog.Builder(this)
                                                val inflater = layoutInflater
                                                val dialogView = inflater.inflate(R.layout.my_success_dialog, null)
                                                builder.setCancelable(false)
                                                builder.setView(dialogView)
                                                val btn_positive = dialogView.findViewById<Button>(R.id.buttonOk)
                                                val dialog = builder.create()
                                                btn_positive.setOnClickListener { dialog.cancel() }
                                                dialog.show()
                                            }
                                            fun uploadIntexFileRecUsingRetrofit(uniqueId: String): Boolean {
                                                val dealerUrl = prefs.getString("DealerUrl", "")
                                                service = Retrofit.Builder().baseUrl(dealerUrl).build().create(UploadService::class.java)
                                                dbhelper = DBHelper(this)
                                                val custNumber = settings.getUserMobileNo().replace("\s+".toRegex(), "")
                                                val DirectoryName = custNumber
                                                val DEFAULT_STORAGE_LOCATION = "/sdcard/Auto Call Record/"
                                                AudioStatusList = ArrayList()
                                                val files = getListFiles(File(DEFAULT_STORAGE_LOCATION), custNumber, DirectoryName)
                                                if (files.isNotEmpty()) {
                                                    for (file in files) {
                                                        val reqFile = RequestBody.create(MediaType.parse("audio*/*"), file)
                                                        val body = MultipartBody.Part.createFormData(
                                                            "audio",
                                                            settings.getDealerId() + "/" + file.name,
                                                            reqFile
                                                        )
                                                        val name: RequestBody = if (uniqueId == null) {
                                                            RequestBody.create(MediaType.parse("text/plain"), "0")
                                                        } else {
                                                            RequestBody.create(MediaType.parse("text/plain"), uniqueId)
                                                        }
                                                        val req: Call<ArrayList<AudioDataResponse>> = service.postImage(body, name)
                                                        req.enqueue(object : Callback<ArrayList<AudioDataResponse>> {
                                                            override fun onResponse(
                                                                call: Call<ArrayList<AudioDataResponse>>,
                                                                response: Response<ArrayList<AudioDataResponse>>
                                                            ) {
                                                                try {
                                                                    println("Response :" + response.message())
                                                                    AudioStatusList = response.body()
                                                                    val status = response.body()?.get(0)?.status
                                                                    val audioFile = response.body()?.get(0)?.filename
                                                                    uploadFirebaseFlag = true
                                                                    try {
                                                                        if (AudioStatusList.size > 0) {
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
                                                                call: Call<ArrayList<AudioDataResponse>>,
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
                                                return uploadFirebaseFlag
                                            }
                                            private fun getListFiles(parentDir: File, custNumber: String, directoryName: String): List<File> {
                                                val inFiles = ArrayList<File>()
                                                val files = parentDir.listFiles()

                                                if (files.isNotEmpty()) {
                                                    for (file in files) {
                                                        if (!file.name.isEmpty()) {
                                                            val fileName = file.name.replace("\\s+".toRegex(), "")
                                                            if (fileName.contains(custNumber) || file.absolutePath.contains(directoryName)) {
                                                                if (file.isDirectory) {
                                                                    val newDirectoryName = file.absolutePath
                                                                    inFiles.addAll(getListFiles(file, custNumber, newDirectoryName))
                                                                } else {
                                                                    if (file.name.endsWith(".mp3")) {
                                                                        val oldFile = File(parentDir, file.name)
                                                                        val latestName = File(parentDir, settings.getFileNameWithUniqueId())
                                                                        val success = oldFile.renameTo(latestName)

                                                                        if (success) {
                                                                            println("file is renamed..")
                                                                        }
                                                                        inFiles.add(latestName)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                return inFiles
                                            }
                                            fun UploadFileUsingRetroFit(uniqueId: String): Boolean {
                                                val dealerUrl = prefs.getString("DealerUrl", "")
                                                val service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
                                                dbhelper = DBHelper(this)
                                                AudioStatusList = ArrayList()
                                                var name: RequestBody
                                                val file = File("$DEFAULT_STORAGE_LOCATION/$uniqueId")
                                                try {
                                                    if (file.exists()) {
                                                        val reqFile = RequestBody.create(MediaType.parse("audio/*"), file)
                                                        val body = MultipartBody.Part.createFormData("audio", file.name, reqFile)
                                                        name = if (uniqueId == null) {
                                                            RequestBody.create(MediaType.parse("text/plain"), "0")
                                                        } else {
                                                            RequestBody.create(MediaType.parse("text/plain"), uniqueId)
                                                        }

                                                        try {
                                                            val req: retrofit2.Call<ArrayList<AudioDataResponse>> = service.postImage(body, name)
                                                            req.enqueue(object : Callback<ArrayList<AudioDataResponse>> {
                                                                override fun onResponse(call: Call<ArrayList<AudioDataResponse>>, response: Response<ArrayList<AudioDataResponse>>) {
                                                                    try {
                                                                        println("Response : ${response.message()}")
                                                                        AudioStatusList = response.body()
                                                                        assert(response.body() != null)
                                                                        val yourFilePath = "$DEFAULT_STORAGE_LOCATION/$uniqueId"
                                                                        val file = File(yourFilePath)
                                                                        val result = file.delete()
                                                                        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                                                                        assert(response.body() != null)
                                                                        val status = response.body()[0].status
                                                                        val audioFile = response.body()[0].filename
                                                                        uploadFirebaseFlag = true
                                                                        try {
                                                                            if (AudioStatusList.size > 0) {
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

                                                                override fun onFailure(call: Call<ArrayList<AudioDataResponse>>, t: Throwable) {
                                                                    t.printStackTrace()
                                                                    println("Error Response : ${t.message}")
                                                                    uploadFirebaseFlag = false
                                                                }
                                                            })

                                                            println("uploadFirebaseFlag : $uploadFirebaseFlag")
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                } catch (fe: Exception) {
                                                    fe.printStackTrace()
                                                }

                                                return uploadFirebaseFlag
                                            }

                                            override fun onPause() {
                                                super.onPause()
                                                settings.setCheckForeground(false)
                                            }

                                            override fun onStop() {
                                                super.onStop()
                                                settings.setCheckForeground(false)
                                            }

                                            override fun onDestroy() {
                                                super.onDestroy()
                                                unregisterNetworkChanges()
                                                settings.setCheckForeground(false)
                                            }
}