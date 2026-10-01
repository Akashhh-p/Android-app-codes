package com.example.intentdemonavigation.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.intentdemonavigation.databinding.FragmentBatteryBinding

class BatteryFragment : Fragment() {
    private var _binding: FragmentBatteryBinding? = null
    private val binding get() = _binding!!

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryInfo(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = requireContext().registerReceiver(batteryReceiver, intentFilter)
        updateBatteryInfo(stickyIntent)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(batteryReceiver)
    }

    private fun updateBatteryInfo(intent: Intent?) {
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()) else -1f
            binding.tvInfoPercentage.text = "Percentage: ${batteryPct.toInt()}%"
            binding.tvInfoScale.text = "Scale: $scale"

            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val statusStr = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                else -> "N/A"
            }
            binding.tvInfoStatus.text = "Status: $statusStr"

            val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val pluggedStr = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                0 -> "Battery"
                else -> "Unknown"
            }
            binding.tvInfoPlugged.text = "Plugged: $pluggedStr"

            val health = it.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val healthStr = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }
            binding.tvInfoHealth.text = "Health: $healthStr"

            val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0
            val volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            binding.tvInfoTemp.text = "Temperature: $temp°C"
            binding.tvInfoVoltage.text = "Voltage: ${volt}mV"

            if (Build.VERSION.SDK_INT >= 36) {
                val capacityLevel = it.getIntExtra(BatteryManager.EXTRA_CAPACITY_LEVEL, -1)
                val capStr = when (capacityLevel) {
                    BatteryManager.BATTERY_CAPACITY_LEVEL_CRITICAL -> "Critical"
                    BatteryManager.BATTERY_CAPACITY_LEVEL_LOW -> "Low"
                    BatteryManager.BATTERY_CAPACITY_LEVEL_NORMAL -> "Normal"
                    BatteryManager.BATTERY_CAPACITY_LEVEL_HIGH -> "High"
                    BatteryManager.BATTERY_CAPACITY_LEVEL_FULL -> "Full"
                    else -> "Unknown"
                }
                binding.tvInfoCapacityLevel.text = "Capacity Level: $capStr"
            } else {
                binding.tvInfoCapacityLevel.text = "Capacity Level: N/A (< API 36)"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val isLow = it.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false)
                binding.tvInfoBatteryLow.text = "Battery Low: $isLow"
            } else {
                binding.tvInfoBatteryLow.text = "Battery Low: N/A"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val cycles = it.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
                binding.tvInfoCycles.text = "Cycle Count: $cycles"
            } else {
                binding.tvInfoCycles.text = "Cycle Count: N/A (< API 34)"
            }

            val batteryManager = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            binding.tvInfoChargeCounter.text = "Charge Counter: $chargeCounter"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}