package com.example.screentimetracker

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.screentimetracker.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var todayAdapter: AppUsageAdapter
    private lateinit var weekAdapter: AppUsageAdapter
    private lateinit var monthAdapter: AppUsageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        setupRecyclerViews()
        setupRefreshButton()

        if (!hasUsageAccess()) {
            showUsageAccessSnackbar()
        } else {
            loadUsageStats()
        }
    }

    private fun setupRecyclerViews() {
        val onAppClick = { packageName: String -> openAppDetails(packageName) }
        
        todayAdapter = AppUsageAdapter(onAppClick)
        weekAdapter = AppUsageAdapter(onAppClick)
        monthAdapter = AppUsageAdapter(onAppClick)

        binding.todayRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = todayAdapter
        }

        binding.weekRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = weekAdapter
        }

        binding.monthRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = monthAdapter
        }
    }

    private fun setupRefreshButton() {
        binding.fabRefresh.setOnClickListener {
            if (hasUsageAccess()) {
                loadUsageStats()
            } else {
                showUsageAccessSnackbar()
            }
        }
    }

    private fun loadUsageStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            val todayStats = getUsageStats(UsageStatsManager.INTERVAL_DAILY)
            val weekStats = getUsageStats(UsageStatsManager.INTERVAL_WEEKLY)
            val monthStats = getUsageStats(UsageStatsManager.INTERVAL_MONTHLY)

            withContext(Dispatchers.Main) {
                todayAdapter.submitList(todayStats)
                weekAdapter.submitList(weekStats)
                monthAdapter.submitList(monthStats)
            }
        }
    }

    private fun getUsageStats(interval: Int): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        when (interval) {
            UsageStatsManager.INTERVAL_DAILY -> calendar.add(Calendar.DAY_OF_MONTH, -1)
            UsageStatsManager.INTERVAL_WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            UsageStatsManager.INTERVAL_MONTHLY -> calendar.add(Calendar.MONTH, -1)
        }
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(interval, startTime, endTime)
        return usageStats
            .filter { it.totalTimeInForeground > 0 }
            .map { AppUsageInfo(it.packageName, it.totalTimeInForeground) }
            .sortedByDescending { it.usageTime }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun showUsageAccessSnackbar() {
        Snackbar.make(
            binding.root,
            R.string.usage_access_required,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.grant_permission) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }.show()
    }

    private fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 