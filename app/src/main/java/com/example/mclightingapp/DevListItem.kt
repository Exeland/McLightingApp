package com.example.mclightingapp

import java.io.Serializable
import java.io.SerializablePermission

data class DevListItem(
    var name: String,
    var ip: String,
    var mac: String
) : Serializable

data class MacAddress(val address: String) {
    fun getAddress(hideMacDetail: Boolean): String {
        if (hideMacDetail) {
            return address.substring(0, "aa:bb:cc".length) + ":XX:XX:XX"
        }
        return address
    }

    val isBroadcast get() = address == "00:00:00:00:00:00"
}
