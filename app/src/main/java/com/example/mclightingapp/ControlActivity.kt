package com.example.mclightingapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.mclightingapp.databinding.ActivityControlBinding
import com.example.mclightingapp.databinding.ActivitySavedDevicesBinding
import com.google.gson.Gson

class ControlActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var currentDevice: DevListItem? = null
    private var preferences: SharedPreferences? = null
    private lateinit var binding: ActivityControlBinding
    private lateinit var actListLauncher: ActivityResultLauncher<Intent>
    var filePath: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = this.getSharedPreferences(WiFiConstants.SAVED_DEVICE_PREFERENCES, Context.MODE_PRIVATE)
        binding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webViewSetup()
        onDeviceListResult()

        currentDevice = restoreDeviceListItem(WiFiConstants.LAST_DEVICE)
        if(currentDevice != null){
            Log.d("MyLog", "Current device: ${currentDevice!!.name.toString()},${currentDevice!!.ip.toString()}")
            webView.loadUrl("http:/${currentDevice!!.ip.toString()}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.control_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_list) {
            actListLauncher.launch(Intent(this, SavedDeviceActivity::class.java))
        } else if (item.itemId == R.id.id_connect) {
            webView.reload()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun webViewSetup(){
        webView = binding.webview
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = MyWebChromeClient(this)
        webView.apply {
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = false;
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
        }
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun onDeviceListResult() {
        actListLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    var device = it.data?.getSerializableExtra(SavedDeviceActivity.DEVICE_KEY) as DevListItem
                    Log.d("MyLog", "${device.name}")
                    if(device != null){
                        currentDevice = device
                        saveDeviceListItem(WiFiConstants.LAST_DEVICE, currentDevice!!)
                        webView.loadUrl("http:/${currentDevice!!.ip.toString()}")
                    }
                }
            }
    }

    private fun restoreDeviceListItem(key: String): DevListItem? {
        val gson = Gson()
        val editor = preferences?.edit()
        var jsonString = preferences?.getString(key, null) ?: return null

        Log.d("MyLog", "saveDeviceList ${key.toString()} jsonString: ${jsonString.toString()} ")

        return gson.fromJson(jsonString, DevListItem::class.java)
    }

    private fun saveDeviceListItem(key: String, item: DevListItem){
        val gson = Gson()
        val editor = preferences?.edit()
        var jsonString = gson.toJson(item)

        Log.d("MyLog", "saveDeviceListItem $key jsonString: $jsonString ")

        editor?.putString(key, jsonString)
        editor?.apply()
    }

    val getFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_CANCELED) {
            filePath?.onReceiveValue(null)
        } else if (it.resultCode == Activity.RESULT_OK && filePath != null) {
            filePath!!.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(it.resultCode, it.data))
            filePath = null
        }
    }

    class MyWebChromeClient(private val myActivity: ControlActivity) : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            myActivity.filePath = filePathCallback

            val contentIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentIntent.type = "*/*"
            contentIntent.addCategory(Intent.CATEGORY_OPENABLE)

            myActivity.getFile.launch(contentIntent)
            return true
        }
    }
}