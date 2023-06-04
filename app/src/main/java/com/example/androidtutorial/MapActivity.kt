package com.example.androidtutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.example.androidtutorial.databinding.ActivityMapBinding
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.*


class MapActivity : AppCompatActivity(), OnMapLongClickListener {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private val pointList = CopyOnWriteArrayList<Feature>()

    private var markerNum= 0
    private var markerWidth = 0
    private var markerHeight = 0

    private val asyncInflater by lazy { AsyncLayoutInflater(this) }
    private val lineList = CopyOnWriteArrayList<Feature>()
    private var lastAddedMarkerPosition: Point? = null

    private companion object {
        const val BLUE_ICON_ID = "blue"
        const val SOURCE_ID = "source_id"
        const val LAYER_ID = "layer_id"
        const val MARKER_ID_PREFIX = "view_annotation_"
        const val LINE_SOURCE_ID = "line_source_id"
        const val LINE_LAYER_ID = "line_layer_id"
        const val DISTANCE_LAYER_ID = "distance_line_layer_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewAnnotationManager = binding.mapView.viewAnnotationManager

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.marker)
        markerWidth = bitmap.width / 2
        markerHeight = bitmap.height / 2

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, markerWidth, markerHeight, false)

        mapboxMap = binding.mapView.getMapboxMap().apply {
            loadStyle(
                styleExtension = prepareStyle(Style.SATELLITE_STREETS, scaledBitmap)
            ) {
                addOnMapLongClickListener(this@MapActivity)
            }
        }
    }

    private fun prepareStyle(styleUri: String, bitmap: Bitmap) = style(styleUri) {
        +image(BLUE_ICON_ID) {
            bitmap(bitmap)
        }
        +geoJsonSource(SOURCE_ID) {
            featureCollection(FeatureCollection.fromFeatures(pointList))
        }
        +symbolLayer(LAYER_ID, SOURCE_ID) {
            iconImage(BLUE_ICON_ID)
            iconAnchor(IconAnchor.BOTTOM)
            iconAllowOverlap(true)
        }
        +geoJsonSource(LINE_SOURCE_ID){
            featureCollection(FeatureCollection.fromFeatures(emptyList()))
        }
        +lineLayer(LINE_LAYER_ID, LINE_SOURCE_ID){
            lineColor("#00FF00")
            lineWidth(4.0)
        }
        +symbolLayer(DISTANCE_LAYER_ID, LINE_SOURCE_ID){
            textField("{distance} m")
            textColor("#000000")
            textAllowOverlap(true)
            textSize(20.0)
            textHaloColor("#0000FF")
            textHaloWidth(4.0)
        }
    }

    override fun onMapLongClick(point: Point): Boolean {
        val markerId = addMarkerAndReturnId(point)
        addViewAnnotation(point, markerId)
        if ( lastAddedMarkerPosition != null){
            addLineAndDistance(lastAddedMarkerPosition!!, point)
        }
        lastAddedMarkerPosition = point
        return true
    }

    private fun addMarkerAndReturnId(point: Point): String {
        val currentId = "${MARKER_ID_PREFIX}${(markerNum++)}"
        pointList.add(Feature.fromGeometry(point, null, currentId))
        val featureCollection = FeatureCollection.fromFeatures(pointList)
        mapboxMap.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
        }
        return currentId
    }

    private fun addViewAnnotation(point: Point, markerId: String) {
        viewAnnotationManager.addViewAnnotation(
            resId = R.layout.view_annotation,
            options = viewAnnotationOptions {
                geometry(point)
                associatedFeatureId(markerId)
                anchor(ViewAnnotationAnchor.BOTTOM)
                allowOverlap(false)
            },
            asyncInflater = asyncInflater
        ) { viewAnnotation ->
            viewAnnotation.visibility = View.VISIBLE
            viewAnnotationManager.updateViewAnnotation(
                viewAnnotation,
                viewAnnotationOptions {
                    offsetY(markerHeight)
                }
            )
            viewAnnotation.findViewById<TextView>(R.id.annotation_txt).text = markerNum.toString()
        }
    }

    private fun calculateDistance(point1: Point, point2: Point): Double{
        val earthRadius = 6378.0
        val lat1 = Math.toRadians(point1.latitude())
        val lon1 = Math.toRadians(point1.longitude())
        val lat2 = Math.toRadians(point2.latitude())
        val lon2 = Math.toRadians(point2.longitude())
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = earthRadius * c
        val distance = distanceKm * 1000

        return distance
    }

    private fun addLineAndDistance(point1: Point, point2: Point){
        val lineCoordinate = mutableListOf(point1, point2)

        val lineString = LineString.fromLngLats(lineCoordinate)

        val distance = calculateDistance(point1,point2)
        val formattedDistance = String.format("%.2f", distance)

        val lineFeature = Feature.fromGeometry(lineString)
        lineFeature.addStringProperty("distance", formattedDistance)

        lineList.add(lineFeature)
        val featureCollection = FeatureCollection.fromFeatures(lineList)

        mapboxMap.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(LINE_SOURCE_ID)?.featureCollection(featureCollection)
        }
    }
}