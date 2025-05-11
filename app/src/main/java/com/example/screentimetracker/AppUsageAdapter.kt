package com.example.screentimetracker

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.screentimetracker.databinding.ItemAppUsageBinding

class AppUsageAdapter(
    private val onAppClick: (String) -> Unit
) : ListAdapter<AppUsageInfo, AppUsageAdapter.ViewHolder>(AppUsageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppUsageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAppUsageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAppClick(getItem(position).packageName)
                }
            }
        }

        fun bind(appUsage: AppUsageInfo) {
            val packageManager = binding.root.context.packageManager
            try {
                val appInfo = packageManager.getApplicationInfo(appUsage.packageName, 0)
                binding.appIcon.setImageDrawable(appInfo.loadIcon(packageManager))
                binding.appName.text = appInfo.loadLabel(packageManager)
                binding.usageTime.text = formatUsageTime(appUsage.usageTime)
            } catch (e: PackageManager.NameNotFoundException) {
                binding.appName.text = appUsage.packageName
                binding.usageTime.text = formatUsageTime(appUsage.usageTime)
            }
        }

        private fun formatUsageTime(timeInMillis: Long): String {
            val hours = timeInMillis / (1000 * 60 * 60)
            val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)
            return when {
                hours > 0 -> binding.root.context.getString(R.string.hours, hours)
                else -> binding.root.context.getString(R.string.minutes, minutes)
            }
        }
    }

    private class AppUsageDiffCallback : DiffUtil.ItemCallback<AppUsageInfo>() {
        override fun areItemsTheSame(oldItem: AppUsageInfo, newItem: AppUsageInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppUsageInfo, newItem: AppUsageInfo): Boolean {
            return oldItem == newItem
        }
    }
} 