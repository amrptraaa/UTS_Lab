package com.example.uts_lab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uts_lab.databinding.HistoryItemBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val historyList: List<historyData>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data ke layout
        fun bind(historyData: historyData) {
            // Load image from imageUrl using Glide
            Glide.with(binding.root.context)
                .load(historyData.imageUrl)
                .into(binding.imageView)

            // Convert timestamp to readable date
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val date = Date(historyData.uploadTime ?: 0)
            val formattedDate = sdf.format(date)

            binding.uploadTimeTextView.text = formattedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size
}
