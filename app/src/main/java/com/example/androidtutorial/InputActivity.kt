package com.example.androidtutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog

class InputActivity : AppCompatActivity() {
    private lateinit var newUserButton: Button
    private lateinit var nameText: EditText
    private lateinit var ageText: EditText

    private lateinit var userArrayList: ArrayList<User>
    private lateinit var userAdapter: InputAdapter
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        newUserButton = findViewById(R.id.newUser_btn)
        newUserButton.setOnClickListener { newUser() }

        nameText = findViewById(R.id.name_txt)
        ageText = findViewById(R.id.age_txt)

        listView = findViewById(R.id.listViewInput)
        userArrayList = ArrayList()

        val name = arrayOf(
            "Juan",
            "Maria",
            "Pedro"
        )
        val age = arrayOf(
            43,
            33,
            25
        )

        for (i in name.indices){
            val user = User(name[i], age[i])
            userArrayList.add(user)
        }

        userAdapter = InputAdapter(this, userArrayList)

        listView.adapter = userAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val userSelected: User = userArrayList[position]

            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Are you sure you want to delete " + userSelected.name)
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    userAdapter.deleteUser(userSelected)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Delete?")
            alert.show()
        }
    }

    private fun newUser(){
        val name: String = nameText.text.toString()
        val age: Int = Integer.parseInt(ageText.text.toString())

        val user = User(name, age)
        userAdapter.addUser(user)
        userAdapter.notifyDataSetChanged()
    }
}