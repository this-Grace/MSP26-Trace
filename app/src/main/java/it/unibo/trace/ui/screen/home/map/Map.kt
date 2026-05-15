package it.unibo.trace.ui.screen.home.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import it.unibo.trace.R
import it.unibo.trace.ui.composable.TraceTopBar
import org.koin.androidx.compose.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Map screen displaying user location using OSMDroid.
 *
 * Handles location permissions, map initialization, and current location overlay.
 *
 * @param navController Navigation controller for back navigation
 * @param viewModel MapScreen ViewModel injected via Koin
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasPermission by viewModel.hasLocationPermission.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updatePermissionStatus(granted)
    }

    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

        val isGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            viewModel.updatePermissionStatus(true)
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TraceTopBar(
                title = stringResource(R.string.map_view_label),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    val locationProvider = GpsMyLocationProvider(context)
                    val locationOverlay = MyLocationNewOverlay(locationProvider, this)
                    if (hasPermission) {
                        locationOverlay.enableMyLocation()
                        locationOverlay.enableFollowLocation()
                    }
                    overlays.add(locationOverlay)
                }
            },
            update = { view ->
                val overlay = view.overlays.filterIsInstance<MyLocationNewOverlay>().firstOrNull()
                if (hasPermission) {
                    overlay?.enableMyLocation()
                    overlay?.enableFollowLocation()
                } else {
                    overlay?.disableMyLocation()
                    overlay?.disableFollowLocation()
                }
            },
            onRelease = { view ->
                view.onPause()
                view.onDetach()
            }
        )
    }
}
