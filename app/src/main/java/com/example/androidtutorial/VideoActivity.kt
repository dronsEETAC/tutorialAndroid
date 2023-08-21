package com.example.androidtutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class VideoActivity : AppCompatActivity(), ClientObserver {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var cannyButton: Button
    private lateinit var grayButton: Button
    private lateinit var normalButton: Button
    private lateinit var imagenView: ImageView
    private lateinit var mqttClient: MqttClientClass
    private var canny : Boolean = false
    private  var gray : Boolean = false

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
        if (OpenCVLoader.initDebug()){
            start()
        }
    }

    private fun normal() {
        gray = false
        canny = false
    }

    private fun gray() {
        gray = true
        canny = false
    }

    private fun canny() {
        gray = false
        canny = true
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
                val bitmap : Bitmap = if(canny||gray){
                    cannyOrGray(decodedByte)
                }else{
                    BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                }
                imagenView.setImageBitmap(bitmap)
            }
            MqttClientClass.newImageChange(false)
        }
    }

    private fun cannyOrGray(byteArray: ByteArray): Bitmap {
        val matOfByte = MatOfByte(*byteArray)
        val mat = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED)
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGBA2GRAY)
        if (canny){
            Imgproc.Canny(mat, mat, 80.0, 100.0)
        }
        val bitmap = Bitmap.createBitmap(mat.cols(),mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        return bitmap
    }
}