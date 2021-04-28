package com.example.fitnessapp.ui.tracker

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.fitnessapp.R
import com.google.android.material.snackbar.Snackbar

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class TrackerFragment : Fragment() {

    private lateinit var trackerViewModel: TrackerViewModel

    private var trackerServiceBound = false

    private var trackerService: TrackerService? = null

    private lateinit var trackerBroadcastReceiver: TrackerBroadcastReceiver

    private lateinit var trackerStartButton: Button

    private lateinit var sharedPreferences: SharedPreferences

    private val trackerServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as TrackerService.LocalBinder
            trackerService = binder.service
            trackerServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            trackerService = null
            trackerServiceBound = false
        }
    }

    private val sharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == TrackerService.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences.getBoolean(
                    TrackerService.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

    private lateinit var fragmentView: View

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        trackerViewModel =
                ViewModelProvider(this).get(TrackerViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tracker, container, false)
//        val textView: TextView = root.findViewById(R.id.text_tracker)
//        trackerViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        trackerBroadcastReceiver = TrackerBroadcastReceiver()

        sharedPreferences =
                activity?.getSharedPreferences(TrackerService.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE) as SharedPreferences

        trackerStartButton = root.findViewById(R.id.tracker_start_button)
        trackerStartButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                    TrackerService.KEY_FOREGROUND_ENABLED, false)

            if (enabled) {
                trackerService?.unsubscribeToLocationUpdates()
            } else {

                // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
                if (foregroundPermissionApproved()) {
                    trackerService?.subscribeToLocationUpdates()
                            ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        }

        fragmentView = root
        return root
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
                sharedPreferences.getBoolean(TrackerService.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        val serviceIntent = Intent(activity as Activity, TrackerService::class.java)
        activity?.bindService(serviceIntent, trackerServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(activity as Activity).registerReceiver(
                trackerBroadcastReceiver,
                IntentFilter(
                        TrackerService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(activity as Activity).unregisterReceiver(
                trackerBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (trackerServiceBound) {
            activity?.unbindService(trackerServiceConnection)
            trackerServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        super.onStop()
    }

    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                activity as Context,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
//            Snackbar.make(
//                    fragmentView.findViewById(R.id.activity_main),
//                    R.string.permission_rationale,
//                    Snackbar.LENGTH_LONG
//            )
//                    .setAction(R.string.ok) {
//                        // Request permission
//                        ActivityCompat.requestPermissions(
//                                this@MainActivity,
//                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//                        )
//                    }
//                    .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                    activity as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    trackerService?.subscribeToLocationUpdates()

                else -> {
                    // Permission denied.
                    updateButtonState(false)

//                    Snackbar.make(
//                            findViewById(R.id.activity_main),
//                            R.string.permission_denied_explanation,
//                            Snackbar.LENGTH_LONG
//                    )
//                            .setAction(R.string.settings) {
//                                // Build intent that displays the App settings screen.
//                                val intent = Intent()
//                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                                val uri = Uri.fromParts(
//                                        "package",
//                                        BuildConfig.APPLICATION_ID,
//                                        null
//                                )
//                                intent.data = uri
//                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                startActivity(intent)
//                            }
//                            .show()
                }
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            trackerStartButton.text = getString(R.string.tracker_stop_tracking)
        } else {
            trackerStartButton.text = getString(R.string.tracker_start_tracking)
        }
    }

    private inner class TrackerBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                    TrackerService.EXTRA_LOCATION
            )

//            if (location != null) {
//                logResultsToScreen("Foreground location: ${location.toText()}")
//            }
        }
    }

    companion object {
        private const val TAG = "TrackerFragment"
    }
}