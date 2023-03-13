package com.example.androidtutorial

import android.app.Activity
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MqttClientClass private constructor() {

    lateinit var client: MqttAndroidClient

    companion object{
        private var mqttInstance:MqttClientClass? = null
        private const val serverURI : String = "tcp://broker.hivemq.com:1883"
        private var clientId = MqttClient.generateClientId()

        fun getMqttInstance(activity: Activity): MqttClientClass {
            if (mqttInstance == null){
                mqttInstance = MqttClientClass()

                mqttInstance!!.client = MqttAndroidClient(activity.applicationContext, serverURI, clientId)

                val options = MqttConnectOptions()
                try {
                    mqttInstance!!.client.connect(options, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d("MQTT Client", "Connection success")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d("MQTT Client", "Connection failure")
                            exception?.printStackTrace()
                        }
                    })
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            }

            return mqttInstance!!
        }
    }
}