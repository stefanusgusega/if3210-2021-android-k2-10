package com.example.fitnessapp.ui.trackerinfo

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.findFragment
import com.example.fitnessapp.FitnessApplication
import com.example.fitnessapp.R
import com.example.fitnessapp.storage.run.Run
import com.example.fitnessapp.ui.tracker.TrackerViewModel.Companion.RUN_MODE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch

class TrackerInfoFragment : Fragment() {

    private lateinit var viewModel: TrackerInfoViewModel

    // Doesn't work :(
//    private val mapReadyCallback = OnMapReadyCallback { googleMap ->
//        // Add polylines to the map.
//        // Polylines are useful to show a route or some other connection between points.
//        val polyline1 = googleMap.addPolyline(
//            PolylineOptions()
//                .clickable(true)
//                .add(
//                    LatLng(-35.016, 143.321),
//                    LatLng(-34.747, 145.592),
//                    LatLng(-34.364, 147.891),
//                    LatLng(-33.501, 150.217),
//                    LatLng(-32.306, 149.248),
//                    LatLng(-32.491, 147.309)))
//
//        // Position the map's camera near Alice Springs in the center of Australia,
//        // and set the zoom factor so most of Australia shows on the screen.
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 4f))
//
//        Log.d(TAG, "Map Ready!")
//        // Set listeners for click events.
////            googleMap.setOnPolylineClickListener(this)
////            googleMap.setOnPolygonClickListener(this)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tracker_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val repository = (activity?.application as FitnessApplication).repository
        val runId = arguments?.getInt("runId") ?: 0

//        viewModel = ViewModelProvider(this).get(TrackerInfoViewModel::class.java)
        viewModel = TrackerInfoViewModelFactory(repository, runId).create(TrackerInfoViewModel::class.java)

        (activity?.application as FitnessApplication).applicationScope.launch {
            val run = viewModel.runInfo.value
            Log.d(TAG, "runId: $runId")
            Log.d(TAG,
                repository.routePointDao.get(0)
                    .joinToString("\n") { rp -> "${rp.pointId}(${rp.latitude}, ${rp.longitude})" })
        }

        // Update text
        viewModel.runInfo.observe(viewLifecycleOwner) { run ->
            val valueText = view?.findViewById(R.id.tracker_info_value) as TextView
            if (run.type == RUN_MODE) {
                valueText.text = "${run.value.toInt().toString()} Steps"
            } else {
                valueText.text = "${"%.2f".format(run.value / 1000)} km"
            }
        }


        // Set map's callback (Doesn't work :( )
//        val mapFragment = activity?.supportFragmentManager?.findFragmentById(R.id.tracker_info_map) //as SupportMapFragment?
//        mapFragment?.getMapAsync(mapReadyCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    companion object {
        private const val TAG = "TrackerInfoFragment"

        fun newInstance() = TrackerInfoFragment()
    }
}