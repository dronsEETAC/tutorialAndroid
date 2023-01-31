package com.example.androidtutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class InputActivity : AppCompatActivity() {
    private lateinit var newUserButton: Button
    private lateinit var nameText: EditText
    private lateinit var ageText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        newUserButton = findViewById(R.id.newUser_btn)
        newUserButton.setOnClickListener { newUser() }

        nameText = findViewById(R.id.name_txt)
        ageText = findViewById(R.id.age_txt)
    }

    private fun newUser(){
        val name: String = nameText.text.toString()
        val age: String = ageText.text.toString()

        println("New user: $name $age")
    }
}