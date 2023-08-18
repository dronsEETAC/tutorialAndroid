package com.example.androidtutorial

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.example.androidtutorial.databinding.ActivityMapBinding
import com.example.androidtutorial.retrofit.FlightPlan
import com.example.androidtutorial.retrofit.FlightPlanClient
import com.example.androidtutorial.retrofit.FlightPlanItem
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
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.CopyOnWriteArrayList
import javax.security.auth.callback.Callback
import kotlin.math.*


class MapActivity : AppCompatActivity(), OnMapLongClickListener {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private val featureList = CopyOnWriteArrayList<Feature>()
    private val pointList = ArrayList<Point>()

    private var markerNum= 0
    private var markerWidth = 0
    private var markerHeight = 0

    private val asyncInflater by lazy { AsyncLayoutInflater(this) }
    private val lineList = CopyOnWriteArrayList<Feature>()
    private var lastAddedMarkerPosition: Point? = null

    private lateinit var saveButton : Button
    private lateinit var loadButton : Button
    private lateinit var clearButton : Button
    private lateinit var pointAdapter : MapAdapter
    private lateinit var listView : ListView

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

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        viewAnnotationManager = binding.mapView.viewAnnotationManager

        saveButton = findViewById(R.id.save_btn)
        saveButton.setOnClickListener { save() }

        loadButton = findViewById(R.id.load_btn)
        loadButton.setOnClickListener { load() }

        clearButton = findViewById(R.id.clear_btn)
        clearButton.setOnClickListener { clear() }

        listView = findViewById(R.id.listViewMap)

        pointAdapter = MapAdapter(this, pointList)

        listView.adapter = pointAdapter

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

    private fun load() {
        clearAll()

        val call = FlightPlanClient.service.getData()

        call.enqueue(object : retrofit2.Callback<List<FlightPlan>> {
            override fun onResponse(call: Call<List<FlightPlan>>, response: Response<List<FlightPlan>>){
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null){
                        val lastFP = data[data.size-1]
                        for (item in lastFP){
                            val point = Point.fromLngLat(item.lng, item.lat)
                            addPoint(point)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<FlightPlan>>, t: Throwable) {
                Log.d("MapActivity Post", t.message.toString())
                Log.d("MapActivity Post", t.stackTraceToString())
            }
        })
    }

    private fun save() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to save flight plan an clear all?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                val flightPlan = FlightPlan()
                for(point in pointList){
                    val item = FlightPlanItem(point.latitude(), point.longitude())
                    flightPlan.add(item)
                }

                val call = FlightPlanClient.service.postFlightPlan(flightPlan)

                call.enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful){
                            clearAll()
                            Toast.makeText(this@MapActivity, "the flight plan has been successfully saved", Toast.LENGTH_LONG)
                        }else{
                            Toast.makeText(this@MapActivity, "the request to the server has not been made correctly", Toast.LENGTH_LONG)

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Handle error response
                        Log.d("MarkerClick Post", t.message.toString())
                        Log.d("MarkerClick Post", t.stackTraceToString())
                    }
                })
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Save Flight Plan")
        alert.show()
    }

    private fun prepareStyle(styleUri: String, bitmap: Bitmap) = style(styleUri) {
        +image(BLUE_ICON_ID) {
            bitmap(bitmap)
        }
        +geoJsonSource(SOURCE_ID) {
            featureCollection(FeatureCollection.fromFeatures(featureList))
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
        addPoint(point)
        return true
    }

    private fun addPoint(point: Point){
        val markerId = addMarkerAndReturnId(point)

        pointAdapter.addPoint(point)
        pointAdapter.notifyDataSetChanged()

        addViewAnnotation(point, markerId, markerNum - 1)
        if ( lastAddedMarkerPosition != null){
            addLineAndDistance(lastAddedMarkerPosition!!, point)
        }
        lastAddedMarkerPosition = point
    }

    private fun addMarkerAndReturnId(point: Point): String {
        val currentId = "${MARKER_ID_PREFIX}${(markerNum++)}"
        featureList.add(Feature.fromGeometry(point, null, currentId))
        val featureCollection = FeatureCollection.fromFeatures(featureList)
        mapboxMap.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featureCollection)
        }
        return currentId
    }

    private fun addViewAnnotation(point: Point, markerId: String, markerNumber: Int) {
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
            viewAnnotation.findViewById<TextView>(R.id.annotation_txt).text = markerNumber.toString()
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

    private fun clearAll(){
        markerNum = 0
        featureList.removeAll(featureList.toSet())
        val featurePointCollection = FeatureCollection.fromFeatures(featureList)
        mapboxMap.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(SOURCE_ID)?.featureCollection(featurePointCollection)
        }

        viewAnnotationManager.removeAllViewAnnotations()

        lastAddedMarkerPosition = null

        lineList.removeAll(lineList.toSet())
        val featureLineCollection = FeatureCollection.fromFeatures(lineList)
        mapboxMap.getStyle { style ->
            style.getSourceAs<GeoJsonSource>(LINE_SOURCE_ID)?.featureCollection(featureLineCollection)
        }

        pointAdapter.delete()
    }

    private fun clear(){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to clear all?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                clearAll()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Clear Markers")
        alert.show()
    }

}