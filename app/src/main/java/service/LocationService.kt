package service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.demo.autosherpa3.WyzConnectApp
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import util.CommonSettings

class LocationService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private var locationManager: LocationManager? = null
    private var mLastLocation: Location? = null

    // Google client to interact with Google API
    private var mGoogleApiClient: GoogleApiClient? = null

    private var mLocationRequest: LocationRequest? = null
    //   private var mRequestingLocationUpdates = false
    companion object {
        const val TAG = "LocationService"
        var UPDATE_INTERVAL = 180000 // 3 min
        var FATEST_INTERVAL = 90000 // 1.5 min
        //public static int DISPLACEMENT = 100; // 100 meters
    }

    private var latitude = 0.0
    private var longitude = 0.0
    private var settings: CommonSettings? = null

    override fun onCreate() {
        super.onCreate()
        settings = applicationContext as WyzConnectApp).settings
        createLocationRequest()
        buildGoogleApiClient()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mGoogleApiClient != null) {
            mGoogleApiClient?.connect()
            Log.d(TAG, "GoogleApiIsConnected")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mGoogleApiClient != null) {
            mGoogleApiClient?.disconnect()
        }
        stopLocationUpdates()
        stopSelf()
        Log.d(TAG, "onDestroy: stopself() service gone")
    }

    /**
     * Creating google api client object
     */
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConnected(bundle: Bundle?) {
        putLocation()
        startLocationUpdates()
        // settings.setUserLocationStatus("On");
//        WyzConnectApp.locationstatusref.setValue(settings.getUserLocationStatus());
        // Log.d(TAG, "userlocationstatus" + settings.getUserLocationStatus());
        Log.d(TAG, "onConnected()")
    }

    override fun onConnectionSuspended(i: Int) {
        mGoogleApiClient?.connect()
        Log.d(TAG, "onConnectionSuspended() ,reconnected")
    }

    override fun onLocationChanged(location: Location?) {
        // Assign the new location
        mLastLocation = location
        putLocation()
        Log.d(TAG, "onLocationChanged()")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        /* settings.setUserLocationStatus("Off");
        WyzConnectApp.locationstatusref.setValue(settings.getUserLocationStatus());
        Log.d(TAG, "userlocationstatus" + settings.getUserLocationStatus());
*/
        //settingsrequest();
        Log.d(TAG, "Location onConnectionFailed()")
    }
    fun startLocationUpdates() {
        Log.d(TAG, "startedLocationUpdates()")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClient, mLocationRequest, this)
    }
    Stopping location updates
    */
    protected fun stopLocationUpdates() {
        Log.d(TAG, "stoppedLocationUpdates()")
        mGoogleApiClient?.let {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                it, this)
        }
    }
    Creating location request object
    */
    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = UPDATE_INTERVAL
        mLocationRequest.fastestInterval = FATEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
// mLocationRequest.smallestDisplacement = DISPLACEMENT // 100 meters
        Log.d(TAG, "createdLocationRequest()")
    }
    private fun putLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (mLastLocation != null) {

            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude

            Log.d(TAG, "Latitude: $latitude")
            Log.d(TAG, "Longitude: $longitude")

            settings.latitude = "$latitude"
            settings.longitude = "$longitude"

            Log.d(TAG, "settings.getLatitude: ${settings.latitude}")
            Log.d(TAG, "settings.getLongitude: ${settings.longitude}")

        } else {
            Log.d(TAG, "mLastLocation is null")
            Toast.makeText(applicationContext, "Couldn't get the location. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show()
        }
    }
}