package io.sunshine0523.sidebar.ui.main

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.sunshine0523.sidebar.R
import io.sunshine0523.sidebar.databinding.ActivityMainBinding
import io.sunshine0523.sidebar.service.SidebarService
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger
import rikka.material.app.MaterialActivity


/**
 * @author KindBrave
 * @since 2023/9/25
 */
class MainActivity : MaterialActivity() {
    private val logger = Logger("MainActivity")
    private lateinit var dataBinding: ActivityMainBinding
    private var hasRequirePermission = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityMainBinding.inflate(layoutInflater)
        dataBinding.toolbar.title = getString(R.string.app_name)
        setContentView(dataBinding.root)
        dataBinding.viewPager.apply {
            adapter = ViewPagerAdapter(this@MainActivity)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    dataBinding.bnv.menu.getItem(position).isChecked = true
                }
            })
            offscreenPageLimit = 1
        }
        dataBinding.bnv.setOnItemSelectedListener {
            dataBinding.viewPager.currentItem = when (it.itemId) {
                //R.id.home -> 0
                else -> 0
            }
            true
        }
//        if (getUsageStatsPermissionState() != AppOpsManager.MODE_ALLOWED) showRequirePermissionDialog()
//        else startService(Intent(this, SidebarService::class.java))
        //不申请权限，直接开启
        startService(Intent(this, SidebarService::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (getUsageStatsPermissionState() != AppOpsManager.MODE_ALLOWED) {
            // 已经申请过权限但没有允许，退出APP
            if (hasRequirePermission) {
                finish()
            }
        } else {
            startService(Intent(this, SidebarService::class.java))
        }
    }

    private fun showRequirePermissionDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.dialog_permission_request))
            setMessage(getString(R.string.dialog_request_permission_message))
            setCancelable(false)
            setPositiveButton("To Authorize") {_, _ ->
                requirePermission()
            }
            setNegativeButton("Cancel") {_, _ ->
                finish()
            }
            show()
        }
    }

    private fun getUsageStatsPermissionState(): Int {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        var mode = AppOpsManager.MODE_ERRORED
        runCatching {
            mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode
    }

    private fun requirePermission(): Boolean {
        hasRequirePermission = true

        return if (getUsageStatsPermissionState() != AppOpsManager.MODE_ALLOWED) {
            Toast.makeText(this, R.string.require_package_usage_stats_permission, Toast.LENGTH_LONG).show()
            runCatching { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
            false
        } else {
            true
        }
    }
}