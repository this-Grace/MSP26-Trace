package it.unibo.trace.ui.screen.home.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the MapScreen.
 *
 * Manages the location permission state for the OSMDroid map view.
 */
class MapViewModel : ViewModel() {
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission = _hasLocationPermission.asStateFlow()

    /**
     * Updates the location permission status.
     *
     * @param granted Whether the location permission has been granted
     */
    fun updatePermissionStatus(granted: Boolean) {
        _hasLocationPermission.value = granted
    }
}
