package com.pon.smsprocessor

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pon.smsprocessor.databinding.ItemListContentBinding


class LogAdapter() :
    RecyclerView.Adapter<LogAdapter.ViewHolder>() {


    var items: List<Logger.LogItem> = emptyList()
        set(value) {
            field=value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemListContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.text.text = items[position].text
        holder.time.text = items[position].time
        if (items[position].important) {
            holder.text.setTextColor(Color.RED)
            holder.time.setTextColor(Color.RED)

        } else {
            holder.text.setTextColor(Color.BLACK)
            holder.time.setTextColor(Color.BLACK)

        }

    }

    override fun getItemCount() = items.size

    inner class ViewHolder(binding: ItemListContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val text: TextView = binding.logItem
        val time: TextView = binding.time
    }

}