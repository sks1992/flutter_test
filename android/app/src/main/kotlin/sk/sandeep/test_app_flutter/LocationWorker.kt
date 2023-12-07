package sk.sandeep.test_app_flutter

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Date

class LocationUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private val db = DatabaseHelper(applicationContext)
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission", "SimpleDateFormat")
    override suspend fun doWork(): Result {
        Log.d("Sandeep WORKER", "doWork: start")
        try {
            if (!applicationContext.hasLocationPermission()) {
                Toast.makeText(
                    applicationContext,
                    "\"Missing location permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d("Sandeep WORKER1", "doWork: start")
            val locationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(applicationContext, "\"GPS is disabled", Toast.LENGTH_SHORT).show()
            }else{
                Log.d("Sandeep WORKER2", "doWork: start")
                val resultLocation = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                )
                Log.d("Sandeep WORKER3", "doWork: start")
                resultLocation.addOnCompleteListener { location ->
                    Log.d("Sandeep WORKER4", "doWork: start")
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())
                    val locationModel =
                        LocationModel(
                            0,
                            location.result.latitude,
                            location.result.longitude,
                            currentDate
                        )
                    Log.d("Sandeep WORKER5", "doWork: Location Save ")
                    db.insertLocation(locationModel)
                    Log.d("Sandeep WORKER6", "doWork: Location Save ")
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}

