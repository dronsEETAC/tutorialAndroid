package com.example.androidtutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class MenuActivity : AppCompatActivity() {
    private lateinit var showAlertButton: Button
    private lateinit var inputButton: Button
    private lateinit var getValueButton: Button
    private lateinit var parametersButton: Button
    private lateinit var videoButton: Button
    private lateinit var mapButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        showAlertButton = findViewById(R.id.showAlert_btn)
        showAlertButton.setOnClickListener { showAlert() }
        inputButton = findViewById(R.id.input_btn)
        getValueButton = findViewById(R.id.getValue_btn)
        parametersButton = findViewById(R.id.parameters_btn)
        videoButton = findViewById(R.id.video_btn)
        mapButton = findViewById(R.id.map_btn)
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
}