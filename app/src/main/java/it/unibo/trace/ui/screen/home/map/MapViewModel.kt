package it.unibo.trace.ui.screen.home.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()

    fun updatePermissionStatus(granted: Boolean) {
        _hasLocationPermission.value = granted
    }
}
