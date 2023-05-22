package com.example.mclightingapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mclightingapp.databinding.ListItemDeviceBinding

class RcAdapter(private val listener: Listener) :
    ListAdapter<DevListItem, RcAdapter.ItemHolder>(ItemComparator()) {

    class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var binding = ListItemDeviceBinding.bind(view)

        fun setData(item: DevListItem, listener: Listener) = with(binding) {
            txtName.text = item.name
            txtIp.text = item.ip
            txtMac.text = item.mac
            itemView.setOnClickListener() {
                listener.onClick(item)
            }
        }

        companion object {
            fun create(parent: ViewGroup): ItemHolder {
                return ItemHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_item_device, parent, false)
                )
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<DevListItem>() {
        override fun areItemsTheSame(oldItem: DevListItem, newItem: DevListItem): Boolean {
            return oldItem.mac == newItem.mac
        }

        override fun areContentsTheSame(oldItem: DevListItem, newItem: DevListItem): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setData(getItem(position), listener)
    }

    interface Listener {
        fun onClick(item: DevListItem)
    }
}