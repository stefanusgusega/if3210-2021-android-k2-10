package com.example.fitnessapp.ui.tracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.fitnessapp.FitnessApplication
import com.example.fitnessapp.MainActivity
import com.example.fitnessapp.R
import com.example.fitnessapp.storage.route.RoutePoint
import com.example.fitnessapp.storage.FitnessRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TrackerService : Service() {

    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private var runId = 0;
    private var pointId = 0;
    private lateinit var repository: FitnessRepository

    private lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate()")

        repository = (application as FitnessApplication).repository

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)

            fastestInterval = TimeUnit.MILLISECONDS.toMillis(500)

            maxWaitTime = TimeUnit.SECONDS.toMillis(4)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.d(TAG, "Location has been received")

                currentLocation = locationResult.lastLocation

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                (application as FitnessApplication).applicationScope.launch {
                    Log.d(TAG, "Location saved to run: ${repository.runDao.getNewRunId()}")
                    val routePoint = RoutePoint(
                        repository.runDao.getNewRunId(),
                        pointId,
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude)
                    repository.insert(routePoint)
                    pointId++
                }

                if (serviceRunningInForeground) {
                    notificationManager.notify(
                            NOTIFICATION_ID,
                            generateNotification(currentLocation)
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")

        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onRebind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange &&
            getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                .getBoolean(KEY_FOREGROUND_ENABLED, false)) {
            Log.d(TAG, "Start foreground service")

            val notification = generateNotification(currentLocation)
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")

        getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, true)
        }

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, this::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE).edit {
                putBoolean(KEY_FOREGROUND_ENABLED, false)
            }
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }

            getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE).edit {
                putBoolean(KEY_FOREGROUND_ENABLED, false)
            }

        } catch (unlikely: SecurityException) {
            getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE).edit {
                putBoolean(KEY_FOREGROUND_ENABLED, true)
            }
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun generateNotification(location: Location?): Notification {
        Log.d(TAG, "generateNotification()")

        // set text
        val titleText = getString(R.string.app_name);
        val mainNotificationText = locationText(location)

        // create channel
        createNotificationChannel()

        // create intents (press actions)
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val activityPendingIntent = PendingIntent.getService(
            this, 0, launchActivityIntent, 0
        )

        // return notification
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(mainNotificationText)
                .setBigContentTitle(titleText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(activityPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun locationText(location : Location?) : String {
        return if (location != null) {
            "(${location.latitude}, ${location.longitude})"
        } else {
            getString(R.string.no_location_text)
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: TrackerService
            get() = this@TrackerService
    }

    companion object {
        private const val TAG = "TrackerService"

        private const val PACKAGE_NAME = "com.example.fitnessapp.ui.tracker"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
                "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
                "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        internal const val PREFERENCE_FILE_KEY = "$PACKAGE_NAME.PREFERENCE_FILE_KEY"

        internal const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

        private const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "tracker_channel_01"
    }
}