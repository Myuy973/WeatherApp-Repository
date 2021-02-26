package com.example.myapiapplication

import android.animation.ObjectAnimator
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapiapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    //APIキー
    val API_KEY = "461d46b9aca2ef84872921d9a0e4c4fe"
    val handler = Handler(Looper.getMainLooper())


    private lateinit var binding : ActivityMainBinding
    private lateinit var weatherListToAdapter: MutableList<Weather>
    // 初期値は北海道
    private var placeLat: Double? = 35.689499
    private var placeLon: Double? = 139.691711




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.hourlyButton.setOnClickListener {
                weatherListToAdapter = mutableListOf<Weather>()
                binding.progressBar.visibility = View.VISIBLE
                get1hWeatherNews()
                Log.d("value", "Start Size: ${weatherListToAdapter.size}")


                thread {
                    while (weatherListToAdapter.size <= 10) {
    //                    delay(500)
    //                    Thread.sleep(1000)
    //                    setLoading(10)
                        android.os.SystemClock.sleep(500)
                        var str = weatherListToAdapter.size.toFloat() / (10 + 1) * 100
                        Log.d("value", "Loading ${str.toInt()} %")
                        handler.post {
                            binding.progressBar.progress = str.toInt()
                        }
                        Log.d("value", "Size: ${weatherListToAdapter.size}")
                    }
                    handler.post{
                        binding.progressBar.apply {
                            visibility = View.INVISIBLE
                            progress = 0
                        }
                        binding.weatherList.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = WeatherAdapter(context, weatherListToAdapter)
                        }
                        Log.d("value", "get1hWeatherNews()  finish!!")

                    }
                }

        }

        binding.dailyButton.setOnClickListener {

                weatherListToAdapter = mutableListOf<Weather>()
                binding.progressBar.visibility = View.VISIBLE
                getDailyWeatherNews()

                thread {
                    while(weatherListToAdapter.size <= 5) {
//                        delay(500)
//                        Thread.sleep(1000)
//                        setLoading(5)
                        android.os.SystemClock.sleep(500)
                        var str = weatherListToAdapter.size.toFloat() / (5 + 1) * 100
                        Log.d("value", "Loading ${str.toInt()} %")
                        handler.post {
                            binding.progressBar.progress = str.toInt()
                        }

                        Log.d("value", "Size: ${weatherListToAdapter.size}")
                    }
                    handler.post {

//                        binding.loadingText.text = "Loading End"
                        binding.progressBar.apply {
                            visibility = View.INVISIBLE
                            progress = 0
                        }
                        binding.weatherList.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = WeatherAdapter(context, weatherListToAdapter)
                        }
                        Log.d("value", "getDailyWeatherNews()  finish!!")

                    }
                }





        }

        binding.placeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    // 項目が選択された場合
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        var spinner = parent as? Spinner
                        when(spinner?.selectedItem as? String) {
                            "北海道" -> latLonSet(43.06451, 141.346603)
                            "東京" ->  latLonSet(35.689499, 139.691711)
                            "掛川" ->  latLonSet(34.76667, 138.016663)
                            "愛知" -> latLonSet(35.180168, 136.906555)
                            "大阪" -> latLonSet(35.950001, 137.266663)
                            "福岡" -> latLonSet(33.606392, 130.41806)
                            else -> latLonSet(35.689499, 139.691711)
                        }
                    }

                    // 項目が選択されずにスピナーが閉じられた場合
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

    }

    private fun setLoading(num: Int) {
        var str = weatherListToAdapter.size.toFloat() / (num + 1) * 100
        Log.d("value", "Loading ${str.toInt()} %")
        onHogeProgressChanged(str.toInt())
    }

    private fun onHogeProgressChanged(loadNum: Int) {

        handler.post {
            val animation = ObjectAnimator.ofInt(binding.progressBar, "progress", loadNum)
            animation.duration = 500
            animation.interpolator = DecelerateInterpolator()
            animation.start()
            Log.d("value", "Loading Set")
        }
    }

    private fun latLonSet(lat: Double, lon: Double) {
        placeLat = lat
        placeLon = lon
    }

    private fun unixtimeChange(time: String, format: String): String {
        var sdf: SimpleDateFormat = SimpleDateFormat(format )
        var nowTime: Date = Date(time.toInt() * 1000L)
        return sdf.format(nowTime)
    }


    private fun get1hWeatherNews(): Job = GlobalScope.launch {

        //アクセスする際のURL
        val API_URL = "https://api.openweathermap.org/data/2.5/onecall?" +
                "lat=" + placeLat + "&" +
                "lon=" + placeLon + "&" +
                "lang=ja&" + "units=metric&" +
                "APPID=" + API_KEY
        var url = URL(API_URL)
        Log.d("value", "url: $API_URL OK")
        val br = BufferedReader(InputStreamReader(url.openStream()))
        val str: String = br.readText()
        var json = JSONObject(str)


        var map = json.getJSONArray("hourly")

        for (i in 0..10) {

            val weather = Weather()
            var firstObject = map.getJSONObject(i)
            var weatherList = firstObject.getJSONArray("weather").getJSONObject(0)

            weather.time = unixtimeChange(firstObject.getString("dt"), "yyyy/MM/dd HH:mm")
            weather.weatherForecast =  weatherList.getString("description")
            weather.temp =  firstObject.getString("temp") + "℃"
            weather.pop =  "${(firstObject.getString("pop").toFloat() * 100).toInt()}%"
            weather.speed =  firstObject.getString("wind_speed") + "km/h"


            var iconName = weatherList.getString("icon")
            var url = "http://openweathermap.org/img/w/$iconName.png"
            Log.d("value", "$url")
            var requestUrl = URL(url)
            var imageRead = requestUrl.openStream()
            weather.image = BitmapFactory.decodeStream(imageRead)

            weatherListToAdapter.add(weather)


        }

    }


    private fun getDailyWeatherNews(): Job = GlobalScope.launch {

        val API_URL = "https://api.openweathermap.org/data/2.5/onecall?" +
                "lat=" + placeLat + "&" +
                "lon=" + placeLon + "&" +
                "lang=ja&" + "units=metric&" +
                "APPID=" + API_KEY
        val url: URL = URL(API_URL)
        Log.d("value", "$url OK")
        val br = BufferedReader(InputStreamReader(url.openStream()))
        val str = br.readText()
        val json = JSONObject(str)

        val daily = json.getJSONArray("daily")
        Log.d("value", "$daily")

        for (i in 0..5) {

            var weather = Weather()
            val firstObject = daily.getJSONObject(i)
            val weatherList = firstObject.getJSONArray("weather").getJSONObject(0)

            weather.time = unixtimeChange(firstObject.getString("dt"), "yyyy/MM/dd")
            weather.weatherForecast = weatherList.getString("description")
            weather.temp = firstObject.getJSONObject("temp").getString("day") + "℃"
            weather.pop =  "${(firstObject.getString("pop").toFloat() * 100).toInt()}%"
            weather.speed = firstObject.getString("wind_speed") + "km/h"


            val iconName = weatherList.getString("icon")
            val url = "http://openweathermap.org/img/w/$iconName.png"
            val requestUrl = URL(url)
            val imageRead = requestUrl.openStream()
            weather.image = BitmapFactory.decodeStream(imageRead)

            weatherListToAdapter.add(weather)

        }


    }



}