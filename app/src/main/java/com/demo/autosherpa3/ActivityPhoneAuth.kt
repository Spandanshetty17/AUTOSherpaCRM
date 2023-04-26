package com.demo.autosherpa3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.demo.autosherpa3.SplashActivity.MY_PREFS_NAME
import com.demo.autosherpa3.SplashActivity.updateUrl
import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hbb20.CountryCodePicker
import java.util.ArrayList
import java.util.HashMap
import java.util.Objects
import java.util.concurrent.TimeUnit
import activity.CREActivity
import butterknife.BindView
import butterknife.ButterKnife
import entity.AppLogin
import entity.UserInfo
import receiver.NetworkReceiver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrointerface.APIClient
import service.UploadService
import util.CallHistory
import util.CommonSettings


class ActivityPhoneAuth : AppCompatActivity() {

    @BindView(R.id.btnSignIN)
    lateinit var btnSignIN: AppCompatButton
    @BindView(R.id.btnSubmit)
    lateinit var btnSubmit: AppCompatButton
    @BindView(R.id.btnVerify)
    lateinit var btnVerify: AppCompatButton
    @BindView(R.id.etxtPhoneCode)
    lateinit var etxtPhoneCode: AppCompatEditText
    @BindView(R.id.etxtPhone)
    lateinit var etxtPhone: AppCompatEditText

    @BindView(R.id.otp_visible)
    lateinit var otp_visible: LinearLayoutCompat
    @BindView(R.id.lnrLogin)
    lateinit var lnrLogin: LinearLayoutCompat
    @BindView(R.id.lnrDealerId)
    lateinit var lnrDealerId: LinearLayoutCompat
    @BindView(R.id.edterror)
    lateinit var edterror: TextInputLayout
    @BindView(R.id.edtdealererror)
    lateinit var edtdealererror: TextInputLayout
    @BindView(R.id.imeierror)
    lateinit var imeierror: TextInputLayout
    @BindView(R.id.edt_imei)
    lateinit var edt_imei: AppCompatEditText
    @BindView(R.id.edtDealerId)
    lateinit var edtDealerId: AppCompatEditText
    private lateinit var btnSignIN: AppCompatButton
    private lateinit var btnSubmit: AppCompatButton
    private lateinit var btnVerify: AppCompatButton
    private lateinit var etxtPhoneCode: AppCompatEditText
    private lateinit var etxtPhone: AppCompatEditText
    private lateinit var otp_visible: LinearLayoutCompat
    private lateinit var lnrLogin: LinearLayoutCompat
    private lateinit var lnrDealerId: LinearLayoutCompat
    private lateinit var edterror: TextInputLayout
    private lateinit var edtdealererror: TextInputLayout
    private lateinit var imeierror: TextInputLayout
    private lateinit var edt_imei: AppCompatEditText
    private lateinit var edtDealerId: AppCompatEditText
    private lateinit var settings: CommonSettings
    private lateinit var ccp: CountryCodePicker
    private lateinit var builder: AlertDialog.Builder
    private lateinit var strRegistrationId: String
    private lateinit var syncsource: String
    private lateinit var authenticationStatus: String
    private lateinit var userId: String
    private lateinit var userRole: String
    private lateinit var userFirstName: String
    private lateinit var userLastName: String
    private lateinit var dealerId: String
    private lateinit var dealerName: String
    private lateinit var userEmail: String
    private lateinit var jwtToken: String
    private lateinit var mNetworkReceiver: BroadcastReceiver
    private lateinit var appPermissions: Array<String>
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124
    private val countryCodeAndroid = "91"
    private val FCM_TOKEN = "FCMToken"
    private var currentVersion: String? = null
    private lateinit var mProgressDialog: ProgressDialog
    private var mVerificationId: String? = null
    private var loginactivity: Activity? = null
    private var databaseReference: DatabaseReference? = null

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)
        ButterKnife.bind(this)
        id_login = findViewById(R.id.id_login)
        settings = CommonSettings.getInstance()
        mNetworkReceiver = NetworkReceiver()
        registerNetworkBroadcastForNougat()
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        editor = prefs.edit()
        appPermissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.CAMERA
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkandrequestPermissions()
            imeierror.visibility = View.VISIBLE
        } else {
            if (checkandrequestPermissions()) {
                getIMEINumber()
            }
            imeierror.visibility = View.GONE
        }

        val extras = intent.extras
        if (extras != null) {
            val latestVersion = extras.getString("LatestVersion")
            currentVersion = extras.getString("CurrentVersion")
            updateUrl = extras.getString("UpdateUrl")
            /if (!latestVersion.equals(currentVersion, ignoreCase = true)) {
                showUpdateDialog()
            }/
        }
        btnSignIN.setOnClickListener { signIn() }
//Note that this will not work on emulator, this requires a real device

        mAuth = FirebaseAuth.getInstance()
        loginactivity = this
        mProgressDialog = ProgressDialog(this)
        ccp = CountryCodePicker(this)
        builder = AlertDialog.Builder(this)
        edtDealerId.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun beforeTextChanged(
                arg0: CharSequence, arg1: Int, arg2: Int,
                arg3: Int
            ) {
            }

            override fun afterTextChanged(et: Editable) {
                var s = et.toString()
                if (!s.equals(s.toUpperCase())) {
                    s = s.toUpperCase()
                    edtDealerId.setText(s)
                    edtDealerId.setSelection(edtDealerId.length()) //fix reverse texting
                }
            }
        })
        btnSubmit.setOnClickListener { v ->
            val edtDealerName = edtDealerId.text.toString()
            dealerName = edtDealerName.replace(" ", "")
            if (!dealerName.isEmpty()) {
                mProgressDialog.isIndeterminate = true
                mProgressDialog.setMessage("Please wait we are connecting...")
                mProgressDialog.show()
                databaseReference = FirebaseDatabase.getInstance().getReference().child("DEALERS URL").child(dealerName)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mProgressDialog.hide()
                        val dealerUrl = dataSnapshot.getValue(String::class.java)
                        if (dealerUrl != null) {
                            editor.clear()
                            editor.commit()
                            editor.putString("DealerUrl", dealerUrl)
                            editor.apply()
                            lnrDealerId.visibility = View.GONE
                            lnrLogin.visibility = View.VISIBLE
                        } else {
                            edtdealererror.error = "Please Enter Valid Dealer Name"
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            } else {
                edtdealererror.error = "Please Enter Dealer Name"
            }
        }

        btnSubmit.setOnClickListener { v ->
            val edtDealerName = edtDealerId.text.toString()
            dealerName = edtDealerName.replace(" ", "")
            if (!dealerName.isEmpty()) {
                mProgressDialog.isIndeterminate = true
                mProgressDialog.setMessage("Please wait we are connecting...")
                mProgressDialog.show()
                databaseReference = FirebaseDatabase.getInstance().getReference().child("DEALERS URL").child(dealerName)
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mProgressDialog.hide()
                        val dealerUrl = dataSnapshot.getValue(String::class.java)
                        if (dealerUrl != null) {
                            editor.clear()
                            editor.commit()
                            editor.putString("DealerUrl", dealerUrl)
                            editor.apply()
                            lnrDealerId.visibility = View.GONE
                            lnrLogin.visibility = View.VISIBLE

                        } else {
                            edtdealererror.setError("Please Enter Valid Dealer Name")
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {


                    }
                })
            } else {
                edtdealererror.setError("Please Enter Dealer Name")
            }


        }

        btnVerify.setOnClickListener { view ->
            if (imeierror.visibility == View.VISIBLE) {
                val imeiNumber = edt_imei.text.toString()
                settings.imeiNumber = if (imeiNumber.isEmpty()) "0" else imeiNumber
            }
            val number = etxtPhone.text?.toString()
            if (number?.length == 10) {
                mProgressDialog.isIndeterminate = true
                mProgressDialog.setTitle(getString(R.string.signing_in))
                mProgressDialog.setMessage("Please wait we are connecting...")
                mProgressDialog.show()
                val phoneNumber = "+$countryCodeAndroid$number"
                val dealerUrl = prefs.getString("DealerUrl", "")
                getLogindetails(phoneNumber, dealerUrl)
            } else {
                edterror.setError("Please Enter 10 Digits Mobile Number")
            }
        }
        btnVerify.setOnClickListener(view -> {
            if (imeierror.getVisibility() == View.VISIBLE) {
                String imeiNumber = edt_imei.getText().toString();
                if (imeiNumber.length() == 0) {
                    settings.setImeiNumber("0");
                } else {
                    settings.setImeiNumber(imeiNumber);
                }
            }
            String number = Objects.requireNonNull(etxtPhone.getText()).toString();
            if (number.length() == 10) {
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setTitle(getString(R.string.signing_in));
                mProgressDialog.setMessage("Please wait we are connecting...");
                mProgressDialog.show();
                String phoneNumber = "+" + countryCodeAndroid + number;
                String dealerUrl = prefs.getString("DealerUrl", "");
                getLogindetails(phoneNumber, dealerUrl);
            } else {
                edterror.setError("Please Enter 10 Digits Mobile Number");
            }
        });


        mAuthListener = firebaseAuth -> {
        };
        ccp.setOnCountryChangeListener(() -> {
            countryCodeAndroid = ccp.getSelectedCountryCode();
            Log.d("Country Code", countryCodeAndroid);
        });
    }

    public static void dialog(boolean value) {
        if (!value) {
            Toast.makeText(loginactivity, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
    fun getLogindetails(phoneNumber: String, dealerUrl: String) {
        Toast.makeText(loginactivity, settings.imeiNumber, Toast.LENGTH_SHORT).show()
        val service = APIClient.getClient(dealerUrl).create(UploadService::class.java)
        val getloginValues = service.getLoginData(phoneNumber, settings.imeiNumber, settings.regToken, currentVersion)
        getloginValues.enqueue(object : Callback<AppLogin> {
            override fun onResponse(call: Call<AppLogin>, response: Response<AppLogin>) {
                mProgressDialog.dismiss()
                if (response.isSuccessful) {
                    try {
                        response.body()?.let {
                            if (it.authenticationStatus.equals("true", ignoreCase = true)) {
                                userId = it.userId
                                userRole = it.userRole
                                userFirstName = it.userFName
                                userLastName = it.userLName
                                dealerId = it.dealerId
                                dealerName = it.dealerName
                                userEmail = it.userEmail as String?
                                jwtToken = it.jwtToken
                                syncsource = it.fireBaseUrl
                                settings.isAuthenticated = true
                                settings.syncSource = "https://autosherpa3-default-rtdb.firebaseio.com/"
                                settings.userId = userId
                                settings.userRole = userRole
                                settings.userFirstName = userFirstName
                                settings.userLirstName = userLastName
                                settings.userEmail = userEmail
                                settings.dealerId = dealerId
                                settings.dealerName = dealerName
                                settings.jwtoken = jwtToken
                                //sending values to firebase
                                val userInfo = UserInfo(settings.dealerId, "false", "Off", CallHistory.PhoneModel, CallHistory.AndroidVersion, currentVersion, phoneNumber, settings.imeiNumber)
                                databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(settings.syncSource + settings.dealerId + "/users/" + settings.userId)
                                databaseReference.setValue(userInfo)

                                if (settings.userRole.equals("CRE", ignoreCase = true)) {
                                    /*requestCode();
                                    lnrLogin.setVisibility(View.GONE);
                                    otp_visible.setVisibility(View.VISIBLE);
                                    mProgressDialog.dismiss();*/
                                    val latestVersion = prefs.getString("LatestVersion", "")
                                    val currentVersion = prefs.getString("CurrentVersion", "")
                                    val updateUrl = prefs.getString("UpdateUrl", "")
                                    val cre_intent = Intent(baseContext, CREActivity::class.java)
                                    cre_intent.putExtra("LatestVersion", latestVersion)
                                    cre_intent.putExtra("CurrentVersion", currentVersion)
                                    cre_intent.putExtra("UpdateUrl", updateUrl)
                                    cre_intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    startActivity(cre_intent)
                                    finish()
                                }
                            } else {
                                edterror.setError("Please enter authorised number")
                                imeierror.setError("Please enter authorised imei number")
                                etxtPhone.setError(null)
                                Log.d("ActivityPhoneAuth", "authentication is false")
                                settings.isAuthenticated = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }

            override fun onFailure(call: Call<AppLogin>, t: Throwable) {
                mProgressDialog.dismiss()
                Log.d("error", t.toString())
                Toast.makeText(loginactivity, "Oops something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signInWithCredential(phoneAuthCredential: PhoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
            .addOnCompleteListener(this) { task ->
                mProgressDialog.dismiss()
                if (task.isSuccessful) {
                    val latestVersion = prefs.getString("LatestVersion", "")
                    val currentVersion = prefs.getString("CurrentVersion", "")
                    val updateUrl = prefs.getString("UpdateUrl", "")
                    val cre_intent = Intent(baseContext, CREActivity::class.java).apply {
                        putExtra("LatestVersion", latestVersion)
                        putExtra("CurrentVersion", currentVersion)
                        putExtra("UpdateUrl", updateUrl)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(cre_intent)
                    finish()
                }
            }
    }

    fun signIn() {
        val code = etxtPhoneCode.text.toString()
        if (TextUtils.isEmpty(code)) {
            etxtPhoneCode.setError("Enter code")
            return
        } else {
            etxtPhoneCode.setError(null)
            signInWithCredential(PhoneAuthProvider.getCredential(mVerificationId, code))
        }
    }

    @TargetApi(23)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            val permissionResults = HashMap<String, Int>()
            var deniedCount = 0
            private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
                mAuth.signInWithCredential(phoneAuthCredential)
                    .addOnCompleteListener(this, task -> {
                mProgressDialog.dismiss();
                if (task.isSuccessful()) {
                    String latestVersion = prefs.getString("LatestVersion", "");
                    String currentVersion = prefs.getString("CurrentVersion", "");
                    String updateUrl = prefs.getString("UpdateUrl", "");
                    Intent cre_intent = new Intent(getBaseContext(), CREActivity.class);
                    cre_intent.putExtra("LatestVersion", latestVersion);
                    cre_intent.putExtra("CurrentVersion", currentVersion);
                    cre_intent.putExtra("UpdateUrl", updateUrl);
                    cre_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    cre_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    cre_intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    cre_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(cre_intent);
                    finish();
                }
            });
            }

            public void signIn() {
                String code = etxtPhoneCode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    etxtPhoneCode.setError("Enter code");
                    return;
                } else {
                    etxtPhoneCode.setError(null);
                    signInWithCredential(PhoneAuthProvider.getCredential(mVerificationId, code));
                }
            }


            @TargetApi(23)
            @Override
            public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
                    HashMap<String, Integer> permissionResults = new HashMap<>();
                    int deniedCount = 0;

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            permissionResults.put(permissions[i], grantResults[i]);
                            deniedCount++;
                        }
                    }

                    if (deniedCount == 0) {

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            getIMEINumber();
                        }


                    } else {
                        for (Map.Entry<String, Integer> entry : permissionResults.entrySet()) {
                            String perName = entry.getKey();

                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perName)) {

                                builder.setMessage("This app need permission to work without problem.")
                                    .setPositiveButton("Yes, Grant Permission", (dialog, which) -> {
                                    dialog.dismiss();
                                    checkandrequestPermissions();
                                })
                                .setNegativeButton("No, exit", (dialog, which) -> {
                                    dialog.dismiss();
                                    finish();
                                }).show();
                                break;
                            } else {

                                builder.setMessage("You've denied some permissions. Allow all permissions at [Settings] > [Permissions]\"")
                                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                                    dialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .setNegativeButton("No Exit", (dialog, which) -> {
                                    dialog.dismiss();
                                    finish();
                                }).show();
                                break;

                            }


                        }
                    }
                }


            }
        }

        fun getIMEINumber() {
            var imeiNumber: String
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                if (phoneStatePermission == PackageManager.PERMISSION_GRANTED) {
                    val mngr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    imeiNumber = mngr.deviceId
                    Log.d("imei number: ", imeiNumber)
                    // Toast.makeText(this, imeiNumber, Toast.LENGTH_LONG).show()
                    settings.setImeiNumber(imeiNumber)
                }
            } else {
                val mngr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                imeiNumber = mngr.deviceId
                Log.d("imei number: ", imeiNumber)
                // Toast.makeText(this, imeiNumber, Toast.LENGTH_LONG).show()
                settings.setImeiNumber(imeiNumber)
            }
        }

        fun checkAndRequestPermissions(): Boolean {
            val listPermissionsNeeded: MutableList<String> = ArrayList()
            for (parm in appPermissions) {
                if (ContextCompat.checkSelfPermission(this, parm) != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(parm)
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
                return false
            }
            return true
        }

        override fun onDestroy() {
            super.onDestroy()
            unregisterNetworkChanges()
        }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menuInflater.inflate(R.menu.cre_menu, menu)
            // change color for icon 0
            val yourdrawable: Drawable = menu.getItem(0).icon // change 0 with 1,2 ...
            yourdrawable.mutate()
            yourdrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.color_background), PorterDuff.Mode.SRC_IN)
            return true
        }


        }
    }
}