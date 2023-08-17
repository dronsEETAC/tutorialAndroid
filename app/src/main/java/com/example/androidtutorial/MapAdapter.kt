package com.example.androidtutorial

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mapbox.geojson.Point

class MapAdapter(private val context: Activity, private val arrayList: ArrayList<Point>):
    ArrayAdapter<Point>(context, R.layout.map_list_item, arrayList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.map_list_item, null)

        val lat: TextView = view.findViewById(R.id.latitud_txt)
        val long: TextView = view.findViewById(R.id.longitud_txt)

        lat.text = String.format("%.2f",arrayList[position].latitude())
        long.text = String.format("%.2f",arrayList[position].longitude())

        return view
    }

    fun addPoint(point: Point){
        arrayList.add(point)
        notifyDataSetChanged()
    }

    fun delete(){
        arrayList.removeAll(arrayList.toSet())
        notifyDataSetChanged()
    }
}