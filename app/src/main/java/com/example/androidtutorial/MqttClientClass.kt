package com.example.androidtutorial

import android.app.Activity
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MqttClientClass private constructor() {

    lateinit var client: MqttAndroidClient
    var messageGetValue: String? = null
    var imageByteArray: ByteArray = byteArrayOf()
    var sendingVideoStream : Boolean = false
    var observers = mutableListOf<ClientObserver>()
    var newImage : Boolean = false

    companion object{
        private var mqttInstance:MqttClientClass? = null
        private const val serverURI : String = "ws://broker.hivemq.com:8000"
        private var clientId = MqttClient.generateClientId()

        private const val TAG = "MQTT Client"

        fun getMqttInstance(activity: Activity): MqttClientClass {
            if (mqttInstance == null){
                mqttInstance = MqttClientClass()

                mqttInstance!!.client = MqttAndroidClient(activity.applicationContext, serverURI, clientId)

                val options = MqttConnectOptions()
                try {
                    mqttInstance!!.client.connect(options, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(TAG, "Connection success")
                            publish("Connect","")
                            subscribe("Value")
                            publish("getValue", "")
                            subscribe("writeParameters")
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(TAG, "Connection failure")
                            exception?.printStackTrace()
                        }
                    })
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
                mqttInstance!!.client.setCallback(object : MqttCallback {
                    override fun messageArrived(topic: String, message: MqttMessage) {
                        Log.d(TAG, "Receive message: $message from topic: $topic")
                        onMessage(message, topic)
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(TAG, "Connection lost ${cause.toString()}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(TAG, "Delivery Complete")
                    }
                })
            }
            return mqttInstance!!
        }
        fun subscribe(topic: String, qos: Int = 1) {
            try {
                mqttInstance!!.client.subscribe(topic, qos, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "Subscribed to $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "Failed to subscribe $topic because ${exception?.printStackTrace()}")
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        private fun onMessage(message: MqttMessage, topic: String){
            when (topic){
                "Value" -> {
                    mqttInstance?.messageGetValue = message.toString()
                }
                "videoFrame" -> {
                    mqttInstance?.imageByteArray = message.payload
                    newImageChange(true)
                }
                "StartVideoStream" -> {
                    subscribe("StopVideoStream")
                    mqttInstance?.sendingVideoStream = true
                }
                "StopVideoStream" -> mqttInstance?.sendingVideoStream = false

            }

        }

        fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
            try {
                val message = MqttMessage()
                message.payload = msg.toByteArray()
                message.qos = qos
                message.isRetained = retained
                mqttInstance!!.client.publish(topic, message, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "$msg published to $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "Failed to publish $msg to $topic")
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }

        fun addObserver(observer: ClientObserver){
            mqttInstance!!.observers.add(observer)
        }

        fun newImageChange(value:Boolean){
            mqttInstance!!.newImage  = value
            mqttInstance!!.observers.forEach { clientObserver ->
                clientObserver.newImage(mqttInstance!!.newImage)
            }

        }
    }
}