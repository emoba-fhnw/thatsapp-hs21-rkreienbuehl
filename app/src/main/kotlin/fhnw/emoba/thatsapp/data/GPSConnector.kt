package fhnw.emoba.thatsapp.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat


/*
 Eintrag im AndroidManifest.xml

     <!-- Zugriff auf den GPS Sensor -->
     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>


 Dependency im build.gradle (emoba.app)

    implementation 'com.google.android.gms:play-services-location:19.0.0'


 Im Emulator
  - in emulator-settings die Location setzen
  - Google Maps aufrufen

*/

class GPSConnector(val activity: Activity) {
    private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                      Manifest.permission.ACCESS_COARSE_LOCATION)

    private val locationProvider by lazy { LocationServices.getFusedLocationProviderClient(activity) }

    init {
        ActivityCompat.requestPermissions(activity, PERMISSIONS, 10)
    }


    @SuppressLint("MissingPermission")
    fun getLocation(onSuccess:          (geoPosition: GeoPosition) -> Unit,
                    onFailure:          (exception: Exception) -> Unit,
                    onPermissionDenied: () -> Unit)  {
        if (PERMISSIONS.oneOfGranted()) {
            locationProvider.lastLocation
                .addOnSuccessListener(activity) {
                    onSuccess.invoke(GeoPosition(it.longitude, it.latitude, it.altitude))
                }

                .addOnFailureListener(activity) {
                    onFailure.invoke(it)
                }
        }
        else {
            onPermissionDenied.invoke()
        }
    }

    private fun Array<String>.oneOfGranted() : Boolean {
        var any = false
        forEach { any = any || it.granted() }

        return any
    }

    private fun String.granted(): Boolean = ActivityCompat.checkSelfPermission(activity, this) == PackageManager.PERMISSION_GRANTED
}

