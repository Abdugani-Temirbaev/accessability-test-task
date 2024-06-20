package com.example.testtask.ui

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testtask.databinding.ActivityMainBinding
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.testtask.R
import com.example.testtask.database.AccountRepository
import com.example.testtask.database.AppDatabase
import com.example.testtask.services.MyAccessibilityService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AccountRepository(AppDatabase.getDatabase(this).accountDao()))
    }

    private val updateUIReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.insertAccount(
                intent?.getStringExtra("accountName") ?: ""
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStart.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                launchInstagram()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.accountName.collect {
                binding.textViewAccountName.text = it
            }
        }

        registerReceiver(
            updateUIReceiver,
            IntentFilter("com.example.testtask.UPDATE_UI")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateUIReceiver)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = ComponentName(this, MyAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service.flattenToString()) == true
    }

    private fun launchInstagram() {
        packageManager.getLaunchIntentForPackage("com.instagram.android")?.let {
            startActivity(it)
        } ?: kotlin.run {
            Toast.makeText(
                this,
                getString(R.string.instagram_not_installed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}