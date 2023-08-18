package com.example.androidtutorial

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketIo {
    lateinit var mSocket: Socket
    fun setSocket() {
        try {
           mSocket = IO.socket("http://10.0.2.2:5000")
        } catch (e: URISyntaxException) {

        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}