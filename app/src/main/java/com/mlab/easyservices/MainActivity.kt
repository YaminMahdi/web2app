package com.mlab.easyservices

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.mlab.easyservices.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val api =
        Retrofit.Builder()
            .baseUrl(InfoAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InfoAPI::class.java)

    lateinit private var sharedPref : SharedPreferences
    lateinit private var editor : SharedPreferences.Editor

    lateinit private var link: String


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("save", Context.MODE_PRIVATE)
        editor = sharedPref.edit()

        link  = sharedPref.getString("url", "https://easyservicesbd.com/")!!

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        binding.webView.settings.useWideViewPort = true
        binding.webView.webViewClient = WebViewClient()

        binding.progressBar.progress = 4
        // Loading a URL
        binding.webView.loadUrl(link)


//        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
//            override fun handleOnBackPressed() {
//                if( binding.webView.canGoBack())
//                    binding.webView.goBack()
//                else
//                    onBackPressedDispatcher.onBackPressed()
//            }
//        })

        MainScope().launch(Dispatchers.IO){
            try {
                val info = api.getWebInfo()
                Log.d("TAG", "onCreate: $info")
                if(info.link != link){
                    editor.putString("url", info.link).apply()
                    link = info.link
                    withContext(Dispatchers.Main){
                        binding.webView.loadUrl(link)
                    }
                }
            }
            catch (ex: Exception){
                ex.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class WebViewClient : android.webkit.WebViewClient() {

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            return if(url.contains("youtube")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                true
            }else if (url.startsWith("http://") || url.startsWith("https://")) {
                binding.progressBar.visibility = View.VISIBLE
                view.loadUrl(url)
                false
            } else try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if( binding.webView.canGoBack())
                    binding.webView.goBack()
                startActivity(intent)
                true
            } catch (e: Exception) {
                Log.i("TAG", "shouldOverrideUrlLoading Exception:$e")
                true
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            MainScope().launch {
                delay(500)
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if( binding.webView.canGoBack())
            binding.webView.goBack()
        else
            super.onBackPressed()
    }
}