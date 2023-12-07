package sk.sandeep.test_app_flutter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.Priority as pro


class MainActivity : FlutterActivity() {
    private val channelName = "Location"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: DatabaseHelper
    private val permissionRequestCode = 0

    private fun setLocationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<LocationUpdateWorker>(10, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LocationUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }


    @SuppressLint("NewApi")
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = DatabaseHelper(context)
        val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "startWorkManger" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        setLocationWorker()
                    }
                    result.success(true)
                }

                "getLastLocation" -> {
                    val allLocationListFromDb = db.getAllLocationData()
                    if (allLocationListFromDb.isEmpty()) {
                        getLastLocation()
                        val allLocationListFromDatabase = db.getAllLocationData()
                        if (allLocationListFromDatabase.isEmpty()) {
                            result.success(
                                "{\"id\":\"${""}\",\"latitude\":\"${""}\",\"longitude\":\"${""}\",\"time\":\"${""}\"}"
                            )
                        } else {
                            val lastLocationList = allLocationListFromDatabase.last()
                            Toast.makeText(
                                this,
                                "first location${lastLocationList.time}",
                                Toast.LENGTH_SHORT
                            ).show()
                            result.success(
                                "{\"id\":\"${lastLocationList.id}\",\"latitude\":\"${lastLocationList.latitude}\",\"longitude\":\"${lastLocationList.longitude}\",\"time\":\"${lastLocationList.time}\"}"
                            )
                        }
                    } else {
                        val locationList = allLocationListFromDb.last()
                        Toast.makeText(
                            this,
                            "last location${locationList.time}",
                            Toast.LENGTH_SHORT
                        ).show()
                        result.success(
                            "{\"id\":\"${locationList.id}\",\"latitude\":\"${locationList.latitude}\",\"longitude\":\"${locationList.longitude}\",\"time\":\"${locationList.time}\"}"
                        )
                    }

                }

                "getLocation" -> {
                    getLastLocation()
                    val allLocationListFromDatabase = db.getAllLocationData()
                    if (allLocationListFromDatabase.isEmpty()) {
                        result.success(
                            "{\"id\":\"${""}\",\"latitude\":\"${""}\",\"longitude\":\"${""}\",\"time\":\"${""}\"}"
                        )
                    } else {
                        val lastLocationList = allLocationListFromDatabase.last()
                        Toast.makeText(
                            this,
                            "first location${lastLocationList.time}",
                            Toast.LENGTH_SHORT
                        ).show()
                        result.success(
                            "{\"id\":\"${lastLocationList.id}\",\"latitude\":\"${lastLocationList.latitude}\",\"longitude\":\"${lastLocationList.longitude}\",\"time\":\"${lastLocationList.time}\"}"
                        )
                    }
                }

                else -> result.notImplemented()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission", "SimpleDateFormat")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                val resultLocation = fusedLocationClient.getCurrentLocation(
                    pro.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                )
                resultLocation.addOnCompleteListener { location ->
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())
                    val locationModel =
                        LocationModel(
                            0,
                            location.result.latitude,
                            location.result.longitude,
                            currentDate
                        )
                    db.insertLocation(locationModel)
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                20
            )
        }
    }

    private fun checkPermissions(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkBackgroundLocation()
            return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionRequestCode
        )
    }

    @SuppressLint("NewApi")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
}


