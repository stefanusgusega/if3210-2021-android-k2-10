package com.example.fitnessapp.ui.scheduler

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ScheduleViewModel(private val repository: ScheduleRepository): ViewModel() {
    val allSchedule: LiveData<List<Schedule>> = repository.allSchedules.asLiveData()

    fun insert(schedule: Schedule) = viewModelScope.launch {
        repository.insert(schedule)
    }
}

class ScheduleViewModelFactory (
    private val repository: ScheduleRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}