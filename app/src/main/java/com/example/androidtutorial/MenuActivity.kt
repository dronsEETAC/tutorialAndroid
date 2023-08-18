package com.example.androidtutorial

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MenuActivity : AppCompatActivity() {
    private lateinit var showAlertButton: Button
    private lateinit var inputButton: Button
    private lateinit var getValueButton: Button
    private lateinit var parametersButton: Button
    private lateinit var videoButton: Button
    private lateinit var mapButton: Button
    private lateinit var mqttClient: MqttClientClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        showAlertButton = findViewById(R.id.showAlert_btn)
        showAlertButton.setOnClickListener { showAlert() }

        inputButton = findViewById(R.id.input_btn)
        inputButton.setOnClickListener { input() }

        getValueButton = findViewById(R.id.getValue_btn)
        getValueButton.setOnClickListener { getValue() }

        parametersButton = findViewById(R.id.parameters_btn)
        parametersButton.setOnClickListener { parameters() }

        videoButton = findViewById(R.id.video_btn)
        videoButton.setOnClickListener { video() }

        mapButton = findViewById(R.id.map_btn)
        mapButton.setOnClickListener { map() }

        mqttClient = MqttClientClass.getMqttInstance(this)

        SocketIo.setSocket()
        SocketIo.establishConnection()

        val mSocket = SocketIo.getSocket()
        mSocket.emit("connectPlatform")

        mSocket.on("connected") {args ->
            if (args[0] != null){
                val msg = args[0] as String
                runOnUiThread{
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setMessage("$msg")
                        .setCancelable(false)
                        .setPositiveButton("Ok"){ dialog, _ ->
                            dialog.cancel()
                        }
                    val alert = dialogBuilder.create()
                    alert.setTitle("SocketIO message")
                    alert.show()
                }
            }

        }

    }

    private fun video() {
        val intent = Intent(this@MenuActivity, VideoActivity::class.java)
        startActivity(intent)
    }

    private fun getValue() {
        var messageMqtt : String? = mqttClient.messageGetValue.toString()
        Toast.makeText(this,"value = $messageMqtt", Toast.LENGTH_LONG).show()
        Log.d("MenuActivity GetValue", "Value: $messageMqtt")
    }

    private fun parameters() {
        val intent = Intent(this@MenuActivity, ParametersActivity::class.java)
        startActivity(intent)
    }

    private fun map() {
        val intent = Intent(this@MenuActivity, MapActivity::class.java)
        startActivity(intent)
    }

    private fun showAlert(){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to close this application?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("AlertDialog")
        alert.show()
    }

    private fun input(){
        val intent = Intent(this@MenuActivity, InputActivity::class.java)
        startActivity(intent)
    }


}