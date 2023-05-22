package com.example.mclightingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mclightingapp.databinding.ActivityScanDevicesBinding

class ScanDevicesActivity : AppCompatActivity(), RcAdapter.Listener {
    private lateinit var binding: ActivityScanDevicesBinding
    private lateinit var adapter: RcAdapter
    private var scanedDeviceList = ArrayList<DevListItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        adapter = RcAdapter(this)
        binding.rcViewScanDev.layoutManager = LinearLayoutManager(this)
        binding.rcViewScanDev.adapter = adapter
        getScannedDevices()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addItem(item: DevListItem) {
        scanedDeviceList.add(item)
        adapter.notifyDataSetChanged()
    }

    private fun getScannedDevices() {
//        scanedDeviceList.add(DevListItem("scanned_1", "192.168.0.1", "00.00.00.00.00.00"))
//        scanedDeviceList.add(DevListItem("scanned_2", "192.168.0.2", "00.00.00.00.00.00"))
//        scanedDeviceList.add(DevListItem("scanned_3", "192.168.0.3", "00.00.00.00.00.00"))
        adapter.submitList(scanedDeviceList)
        ScannerDevice().startScan(this, ::addItem)
    }

    companion object {
        const val DEVICE_KEY = "device_key"
    }

    override fun onClick(item: DevListItem) {
        val i = Intent().apply {
            putExtra(ScanDevicesActivity.DEVICE_KEY, item)
        }

        setResult(RESULT_OK, i)
        finish()
    }
}