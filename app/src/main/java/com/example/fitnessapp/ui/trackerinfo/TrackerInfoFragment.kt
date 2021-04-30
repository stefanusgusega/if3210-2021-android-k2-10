package com.example.fitnessapp.ui.trackerinfo

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fitnessapp.FitnessApplication
import com.example.fitnessapp.R
import com.example.fitnessapp.storage.run.Run
import com.example.fitnessapp.ui.tracker.TrackerViewModel.Companion.RUN_MODE
import kotlinx.coroutines.launch

class TrackerInfoFragment : Fragment() {

    private lateinit var viewModel: TrackerInfoViewModel

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
            val valueText = view?.findViewById<TextView>(R.id.tracker_info_value) as TextView
            if (run.type == RUN_MODE) {
                valueText.text = "${run.value.toInt().toString()} Steps"
            } else {
                valueText.text = "${"%.2f".format(run.value / 1000)} km"
            }
        }
    }


    companion object {
        private const val TAG = "TrackerInfoFragment"

        fun newInstance() = TrackerInfoFragment()
    }
}