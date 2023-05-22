package com.example.mclightingapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mclightingapp.databinding.ActivitySavedDevicesBinding
import com.google.gson.Gson

class SavedDeviceActivity : AppCompatActivity(), RcAdapter.Listener {
    private var preferences: SharedPreferences? = null
    private var launcher: ActivityResultLauncher<Intent>? = null
    private lateinit var binding: ActivitySavedDevicesBinding
    private lateinit var adapter: RcAdapter
    private var savedDeviceList = ArrayList<DevListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = this.getSharedPreferences(WiFiConstants.SAVED_DEVICE_PREFERENCES, Context.MODE_PRIVATE)
        binding = ActivitySavedDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val item: DevListItem =
                        result.data?.getSerializableExtra("device_key") as DevListItem
                    Log.d("MyLog", "Selected ${item.name}")
                    addItem(item)
                }
            }

        init()
    }

    private fun init() {
        adapter = RcAdapter(this)
        binding.rcViewSavedDev.layoutManager = LinearLayoutManager(this)
        binding.rcViewSavedDev.adapter = adapter
        binding.btnAddDevice.setOnClickListener {
            launcher?.launch(Intent(this, ScanDevicesActivity::class.java))
        }

        getSavedDevices()
    }

    private fun addItem(item: DevListItem) {
        if(item in savedDeviceList) return

        savedDeviceList.add(item);
        adapter.notifyDataSetChanged();
        saveDeviceList(savedDeviceList)
    }

    private fun getSavedDevices() {
        restoreDeviceList(savedDeviceList)
        adapter.submitList(savedDeviceList)
    }

    companion object {
        const val DEVICE_KEY = "device_key"
    }

    private fun restoreDeviceList(list: ArrayList<DevListItem>) {
        var i: Int = 1
        do{
            var key = "${WiFiConstants.DEVICE_LIST_ITEM}_${i.toString()}"
            var item = restoreDeviceListItem(key)
            if(item != null) list.add(item)
            i++
        }while (item != null)
    }

    private fun saveDeviceList(list: ArrayList<DevListItem>) {
        for (item in list){
            var i = list.indexOf(item) + 1
            var key:String = "${WiFiConstants.DEVICE_LIST_ITEM}_${i.toString()}"
            saveDeviceListItem(key, item)
        }
    }

    private fun saveDeviceListItem(key: String, item: DevListItem){
        val gson = Gson()
        val editor = preferences?.edit()
        var jsonString = gson.toJson(item)

        Log.d("MyLog", "saveDeviceListItem $key jsonString: $jsonString ")

        editor?.putString(key, jsonString)
        editor?.apply()
    }

    private fun restoreDeviceListItem(key: String): DevListItem? {
        val gson = Gson()
        val editor = preferences?.edit()
        var jsonString = preferences?.getString(key, null) ?: return null

        Log.d("MyLog", "saveDeviceList ${key.toString()} jsonString: ${jsonString.toString()} ")

        return gson.fromJson(jsonString, DevListItem::class.java)
    }

    override fun onClick(item: DevListItem) {
        val i = Intent().apply {
            putExtra(DEVICE_KEY, item)
        }

        setResult(RESULT_OK, i)
        finish()
    }
}


