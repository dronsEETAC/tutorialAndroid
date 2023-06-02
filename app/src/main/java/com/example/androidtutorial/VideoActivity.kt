package com.example.androidtutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class VideoActivity : AppCompatActivity(), ClientObserver {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var cannyButton: Button
    private lateinit var grayButton: Button
    private lateinit var normalButton: Button
    private lateinit var imagenView: ImageView
    private lateinit var mqttClient: MqttClientClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        startButton = findViewById(R.id.start_btn)
        startButton.setOnClickListener { start() }
        stopButton = findViewById(R.id.stop_btn)
        stopButton.setOnClickListener { stop() }
        cannyButton = findViewById(R.id.canny_btn)
        cannyButton.setOnClickListener { canny() }
        grayButton = findViewById(R.id.gray_btn)
        grayButton.setOnClickListener { gray() }
        normalButton = findViewById(R.id.normal_btn)
        normalButton.setOnClickListener { normal() }

        imagenView = findViewById(R.id.image_view)

        mqttClient = MqttClientClass.getMqttInstance(this)
        MqttClientClass.addObserver(this)
        start()

    }

    private fun normal() {

    }

    private fun gray() {

    }

    private fun canny() {

    }

    private fun stop() {
        MqttClientClass.publish("StopVideoStream", "")
    }

    private fun start() {
        MqttClientClass.publish("StartVideoStream", "")
        MqttClientClass.subscribe("videoFrame")
    }

    override fun newImage(value: Boolean) {
        if (mqttClient.newImage){
            runOnUiThread{
                val decodedByte = Base64.decode(mqttClient.imageByteArray, Base64.DEFAULT)
                val bitmap : Bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                imagenView.setImageBitmap(bitmap)
            }
            MqttClientClass.newImageChange(false)
        }

    }
}