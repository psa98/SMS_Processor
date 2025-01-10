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

        holder.text.text = "${items[position].time} \n${items[position].text}"
        if (items[position].important) {
            holder.text.setTextColor(Color.RED)
        } else {
            holder.text.setTextColor(Color.BLACK)
        }

    }

    // хак повзволяющий выделять строки в ресайклере
    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.text.setEnabled(false);
        holder.text.setEnabled(true);
    }


    override fun getItemCount() = items.size

    inner class ViewHolder(binding: ItemListContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val text: TextView = binding.text

    }

}