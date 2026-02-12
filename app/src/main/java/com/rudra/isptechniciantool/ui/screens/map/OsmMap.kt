package com.rudra.isptechniciantool.ui.screens.map

import android.content.Context
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.rudra.isptechniciantool.domain.model.Device
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Composable wrapper for OSMDroid MapView.
 * Handles lifecycle and device marker display.
 */
@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    devices: List<Device>,
    selectedDevice: Device?,
    mapCenterLatitude: Double,
    mapCenterLongitude: Double,
    zoomLevel: Double,
    showDeviceLabels: Boolean,
    onMapCenterChanged: (Double, Double) -> Unit,
    onZoomChanged: (Double) -> Unit,
    onDeviceSelected: (Device?) -> Unit,
    onMapLongPress: (Double, Double) -> Unit,
    onAddDeviceAtLocation: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Load OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            
            // Set initial position
            controller.setZoom(zoomLevel)
            controller.setCenter(GeoPoint(mapCenterLatitude, mapCenterLongitude))

            // Map listener for center changes
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    val center = mapCenter
                    onMapCenterChanged(center.latitude, center.longitude)
                    return true
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    onZoomChanged(zoomLevelDouble)
                    return true
                }
            })

            // Long press for adding device
            setOnLongClickListener { e ->
                val projection = projection
                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                onMapLongPress(geoPoint.latitude, geoPoint.longitude)
                true
            }
        }
    }

    // Lifecycle handling
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
            mapView.onDetach()
        }
    }

    // Update device markers
    LaunchedEffect(devices, showDeviceLabels) {
        mapView.overlays.removeAll { it is Marker || it is DeviceMarker }

        devices.forEach { device ->
            if (device.latitude != null && device.longitude != null) {
                val shouldShow = showDeviceLabels || device == selectedDevice
                if (shouldShow) {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(device.latitude, device.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = device.name
                        snippet = device.deviceType.name

                        setOnMarkerClickListener { _, _ ->
                            onDeviceSelected(device)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
            }
        }
        mapView.invalidate()
    }

    // Handle center position updates
    LaunchedEffect(mapCenterLatitude, mapCenterLongitude, zoomLevel) {
        val currentCenter = mapView.mapCenter
        if (currentCenter.latitude != mapCenterLatitude || 
            currentCenter.longitude != mapCenterLongitude ||
            mapView.zoomLevelDouble != zoomLevel) {
            mapView.controller.setZoom(zoomLevel)
            mapView.controller.setCenter(GeoPoint(mapCenterLatitude, mapCenterLongitude))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
