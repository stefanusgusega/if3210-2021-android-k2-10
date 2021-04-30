package com.example.fitnessapp.ui.tracker

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.example.fitnessapp.FitnessApplication
import com.example.fitnessapp.R
import com.example.fitnessapp.storage.FitnessRepository
import com.example.fitnessapp.storage.route.RoutePoint
import com.example.fitnessapp.storage.run.Run
import com.example.fitnessapp.ui.tracker.TrackerViewModel.Companion.BIKE_MODE
import com.example.fitnessapp.ui.tracker.TrackerViewModel.Companion.RUN_MODE
import com.example.fitnessapp.ui.trackerinfo.TrackerInfoFragment
import kotlinx.coroutines.launch
import kotlin.math.PI

class TrackerFragment : Fragment() {

    private lateinit var repository: FitnessRepository

    private lateinit var trackerViewModel: TrackerViewModel

    private var trackerServiceBound = false

    private var trackerService: TrackerService? = null

    private lateinit var trackerBroadcastReceiver: TrackerBroadcastReceiver

    private lateinit var trackerStartButton: Button

    private lateinit var sharedPreferences: SharedPreferences

    private var runId = 0

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
        (activity?.application as FitnessApplication).applicationScope.launch {
            // Updates button states if new while in use location is added to SharedPreferences.
            if (key == TrackerService.KEY_FOREGROUND_ENABLED) {
                updateButtonState(sharedPreferences.getBoolean(
                        TrackerService.KEY_FOREGROUND_ENABLED, false)
                )
                if (!sharedPreferences.getBoolean(
                        TrackerService.KEY_FOREGROUND_ENABLED, false)) {

                    if (trackerViewModel.state.value == RUN_MODE) {
                        repository.runDao.insert(Run(
                            runId,
                            RUN_MODE,
                            stepReading - sharedPreferences.getFloat(KEY_STEP_START, stepReading)
                        ))
                    } else {
                        var sum = 0.0;
                        var prevPoint = RoutePoint(0, -1, 0.0, 0.0)
                        Log.d(TAG, "RoutePoints: ${repository.routePointDao.get(runId).size}")
                        repository.routePointDao.get(runId).forEach {
                            if (prevPoint.pointId != -1) {
                                val locationA = Location("A")
                                locationA.latitude = prevPoint.latitude
                                locationA.longitude = prevPoint.longitude
                                val locationB = Location("B")
                                locationB.latitude = it.latitude
                                locationB.longitude = it.longitude

                                sum += locationA.distanceTo(locationB)
                            }
                            prevPoint = it
                        }
                        Log.d(TAG, "Distance: $sum")
                        repository.runDao.insert(Run(
                            runId,
                            BIKE_MODE,
                            sum.toFloat()
                        ))
                    }

                    // countDistance here
                }
            }
        }
        if (key == TrackerService.KEY_FOREGROUND_ENABLED) {
            if (!sharedPreferences.getBoolean(
                            TrackerService.KEY_FOREGROUND_ENABLED, false)) {
                val bundle = bundleOf("runId" to runId)
                view?.findNavController()?.navigate(R.id.next_action, bundle)
            }
        }
    }

    private lateinit var compassImageView: ImageView

    private lateinit var sensorManager: SensorManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var stepReading = 0f

    private val sensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                if (!sharedPreferences.contains(KEY_STEP_START)) {
                    sharedPreferences.edit {
                        putFloat(KEY_STEP_START, event.values[0])
                    }
                    stepReading = event.values[0]
                }
            }

            updateOrientationAngles()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Nothing
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
                if (trackerViewModel.state.value != RUN_MODE) {
                    if (foregroundPermissionApproved()) {
                        trackerService?.subscribeToLocationUpdates()
                            ?: Log.d(TAG, "Service Not Bound")
                    } else {
                        requestForegroundPermissions()
                    }
                } else {
                    sharedPreferences.edit {
                        putBoolean(TrackerService.KEY_FOREGROUND_ENABLED, true)
                    }
                }
            }
        }

        // Setup sensors
        compassImageView = root.findViewById(R.id.compassImage)
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager

        val trackerModeText = root.findViewById<TextView>(R.id.tracker_mode_text)
        // Setup buttons
        root.findViewById<ImageButton>(R.id.bike_mode_button).setOnClickListener {
            trackerViewModel.state.value = BIKE_MODE
            trackerModeText.text = getString(R.string.tracker_mode_bike)
        }
        root.findViewById<ImageButton>(R.id.run_mode_button).setOnClickListener {
            trackerViewModel.state.value = RUN_MODE
            trackerModeText.text = getString(R.string.tracker_mode_run)
        }
        trackerModeText.text =
            if (trackerViewModel.state.value == RUN_MODE) getString(R.string.tracker_mode_run)
            else getString(R.string.tracker_mode_bike)

        fragmentView = root
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        repository = (activity?.application as FitnessApplication).repository
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

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                sensorEventListener,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.also { step ->
            sensorManager.registerListener(
                sensorEventListener,
                step,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        (activity?.application as FitnessApplication).applicationScope.launch {
            runId = repository.runDao.getNewRunId()
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(activity as Activity).unregisterReceiver(
                trackerBroadcastReceiver
        )
        super.onPause()

        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onStop() {
        if (trackerServiceBound) {
            activity?.unbindService(trackerServiceConnection)
            trackerServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)

        super.onStop()
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                activity as Context,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (!provideRationale) {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

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

    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "orientationAngles" now has up-to-date information.

        compassImageView.rotation = (orientationAngles[0] * 180 / PI).toFloat()
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
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val KEY_STEP_START = "tracking_step_start"
    }
}