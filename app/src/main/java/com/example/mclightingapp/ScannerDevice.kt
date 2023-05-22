package com.example.mclightingapp

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mclightingapp.util.inet4AddressFromInt
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException


class ScannerDevice {
    private lateinit var activity: AppCompatActivity
    private lateinit var scanThread: Thread
    private lateinit var request: Request
    private val client = OkHttpClient()

    companion object {
        val TAG: String = ScannerDevice::class.java.name
    }

    fun startScan(activity: AppCompatActivity, addItem: (item: DevListItem) -> Unit) {
        this.activity = activity

        scanThread = Thread() {
            val netInterfaces = NetworkInterface.getNetworkInterfaces()

            for (ni in netInterfaces){
                for (addr in ni.interfaceAddresses){
                    val nwAdr = addr.address
                    val baseIp =  inet4AddressFromInt(
                        "",
                        nwAdr.hashCode() and 0xffffff00.toInt()
                    )

                    if (nwAdr.isLoopbackAddress || (nwAdr !is Inet4Address)) {
                        continue
                    }

                    for (i in 1..254) {
                        var adrs = inet4AddressFromInt("",baseIp.hashCode() + i)

                        val isReachable = try {
                            adrs.isReachable(200)
                        } catch (ex: SocketException) {
                            ex.printStackTrace()
                            false
                        }

                        if (activity.isDestroyed) {
                            Log.d("MyLog", "ScannerDevice thread closed")
                            return@Thread
                        }

                        Log.d("MyLog", "Adress: ${adrs.toString()} ${isReachable.toString()}")

                        if (!isReachable) continue

                        var mac = ""
                        var response = requestGet(adrs, "run?cmd=getdevtype")
                        val txt = response?.string()

                        Log.d("MyLog", "MAC: $mac")
                        Log.d("MyLog", "Response: $txt")

                        if (txt != "McLighting") continue

                        var name = "scanned"
                        response = requestGet(adrs, "run?cmd=getname")
                        name = response?.string().toString()

                        if(name.isNullOrBlank()) continue

                        activity.runOnUiThread {
                            addItem(DevListItem(name, adrs.toString(), mac))
                        }

                    }

                }
            }

        }
        scanThread.start()
    }

    private fun requestGet(adrs: Inet4Address, message: String) : ResponseBody? {
        var url = "http:/${adrs.toString()}/$message"

        Log.d("MyLog", "Send Request: $url")
        request = Request.Builder().url(url).build()

        return try {
            var response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (i: IOException) {
            null
        }
    }

    sealed class ScanProgress {
        object ScanNotStarted : ScanProgress()
        data class ScanRunning(val progress: Double) : ScanProgress()
        object ScanFinished : ScanProgress()


        operator fun plus(progress: Double) = when (this) {
            is ScanNotStarted -> ScanRunning(
                progress
            )
            is ScanRunning -> ScanRunning(
                this.progress + progress
            )
            is ScanFinished -> ScanFinished
        }
    }
}