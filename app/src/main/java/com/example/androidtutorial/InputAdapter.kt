package com.example.androidtutorial

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class InputAdapter(private val context: Activity, private val arrayListUser: ArrayList<User>):
    ArrayAdapter<User>(context, R.layout.list_item_input, arrayListUser) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.list_item_input, null)

        val userName: TextView = view.findViewById(R.id.userName)
        val userAge: TextView = view.findViewById(R.id.userAge)

        userName.text = arrayListUser[position].name
        userAge.text = arrayListUser[position].age.toString()

        return view
    }

    fun addUser(user: User){
        arrayListUser.add(user)
        notifyDataSetChanged()
    }

    fun deleteUser(selectedUser: User){
        arrayListUser.remove(selectedUser)
        notifyDataSetChanged()
    }
}