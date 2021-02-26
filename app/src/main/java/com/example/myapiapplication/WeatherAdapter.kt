package com.example.myapiapplication

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(
    private val context: Context,
    private val weather: List<Weather>
) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.timeText)
        val image: ImageView = view.findViewById(R.id.imageView)
        val weather: TextView = view.findViewById(R.id.weatherText)
        val temp: TextView = view.findViewById(R.id.tempText)
        val pop: TextView = view.findViewById(R.id.popText)
        val speed: TextView = view.findViewById(R.id.speedText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.time.text = weather[position].time
        holder.image.setImageBitmap(weather[position].image)
        holder.weather.text = weather[position].weatherForecast
        holder.temp.text = weather[position].temp
        holder.pop.text = weather[position].pop
        holder.speed.text = weather[position].speed

    }

    override fun getItemCount(): Int = weather.size

}