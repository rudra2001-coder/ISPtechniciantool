package com.rudra.isptechniciantool.ui.screens.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.rudra.isptechniciantool.R
import com.rudra.isptechniciantool.domain.model.Device
import com.rudra.isptechniciantool.domain.model.DeviceStatus
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * Custom marker overlay for displaying devices on the OSMDroid map.
 */
class DeviceMarker(
    private val mapView: MapView,
    private val device: Device,
    private val onMarkerClick: (Device) -> Boolean
) : Overlay() {

    private val statusColor: Int
        get() = when (device.status) {
            DeviceStatus.ONLINE -> Color.parseColor("#4CAF50") // Green
            DeviceStatus.OFFLINE -> Color.parseColor("#F44336") // Red
            DeviceStatus.UNKNOWN -> Color.parseColor("#9E9E9E") // Gray
            DeviceStatus.DEGRADED -> Color.parseColor("#FF9800") // Orange
            DeviceStatus.MAINTENANCE -> Color.parseColor("#2196F3") // Blue
        }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return

        val point = mapView.projection.toPixels(
            GeoPoint(device.latitude ?: 0.0, device.longitude ?: 0.0),
            null
        )

        val radius = 20f
        val circle = OvalShape().apply {
            resize(
                radius * 2,
                radius * 2
            )
        }

        val drawable = ShapeDrawable(circle).apply {
            paint.color = statusColor
            paint.style = android.graphics.Paint.Style.FILL
            setBounds(
                (point.x - radius).toInt(),
                (point.y - radius).toInt(),
                (point.x + radius).toInt(),
                (point.y + radius).toInt()
            )
        }
        drawable.draw(canvas)

        // Draw border
        val borderPaint = android.graphics.Paint().apply {
            color = Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), radius, borderPaint)
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        val point = mapView.projection.toPixels(
            GeoPoint(device.latitude ?: 0.0, device.longitude ?: 0.0),
            null
        )

        val radius = 25f
        val distance = Math.sqrt(
            Math.pow((e.x - point.x).toDouble(), 2.0) +
            Math.pow((e.y - point.y).toDouble(), 2.0)
        )

        return if (distance <= radius) {
            onMarkerClick(device)
        } else {
            false
        }
    }
}
