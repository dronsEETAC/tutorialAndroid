package com.example.androidtutorial

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var connectButton: Button
    private var connect: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.connect_btn)
        connectButton.setBackgroundColor(Color.parseColor("#FF0000"))
        connectButton.setOnClickListener{connect()}
    }

    private fun connect(){
        if (connect){
            connectButton.text = "Connect"
            connectButton.setBackgroundColor(Color.parseColor("#FF0000"))
            connect = false
        }
        else{
            connectButton.text = "Disconnect"
            connectButton.setBackgroundColor(Color.parseColor("#ff669900"))
            connect = true
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }
}