package com.example.androidtutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar

class ParametersActivity : AppCompatActivity() {
    private var radioGroup: RadioGroup? = null
    private lateinit var sendButton: Button
    private lateinit var seekBar: SeekBar
    private lateinit var uno: CheckBox
    private lateinit var dos: CheckBox
    private lateinit var tres: CheckBox
    private lateinit var cuatro: CheckBox
    private lateinit var radioButton: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters)

        radioGroup = findViewById(R.id.radioGroup)

        uno = findViewById(R.id.checkBox1)
        dos = findViewById(R.id.checkBox2)
        tres = findViewById(R.id.checkBox3)
        cuatro = findViewById(R.id.checkBox4)

        seekBar = findViewById(R.id.seekBar)

        sendButton = findViewById(R.id.sendParameters_btn)
        sendButton.setOnClickListener { sendParameters() }
    }

    private fun sendParameters() {
        val selectBtn: Int = radioGroup!!.checkedRadioButtonId
        radioButton = findViewById(selectBtn)

        val result = StringBuilder()
        if(uno.isChecked){
            result.append("uno ")
        }
        if(dos.isChecked){
            result.append("dos ")
        }
        if(tres.isChecked){
            result.append("tres ")
        }
        if(cuatro.isChecked){
            result.append("cuatro ")
        }

        Log.d("ParametersActivity send", "RadioButton: ${radioButton.text}, CheckBox: $result, SeekBar: ${seekBar.progress}")

    }
}