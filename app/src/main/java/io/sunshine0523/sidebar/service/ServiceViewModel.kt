package io.sunshine0523.sidebar.service

import android.app.Application
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.sunshine0523.sidebar.R
import io.sunshine0523.sidebar.app.SidebarApplication
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.room.DatabaseRepository
import io.sunshine0523.sidebar.systemapi.UserHandleHidden
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger
import io.sunshine0523.sidebar.utils.getInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Collections
import java.util.Locale


/**
 * @author KindBrave
 * @since 2023/8/25
 */
class ServiceViewModel(private val application: Application): AndroidViewModel(application) {
    private val logger = Logger("ServiceViewModel")

    private val repository = DatabaseRepository(application)
    private val sp = application.applicationContext.getSharedPreferences(SidebarApplication.CONFIG, Context.MODE_PRIVATE)

    val sidebarAppListFlow: Flow<List<AppInfo>>
        get() = _sidebarAppList
    private val _sidebarAppList = MutableStateFlow<ArrayList<AppInfo>>(ArrayList())

    private val launcherApps: LauncherApps = application.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val usageStatsManager = application.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val lastTimeUsedComparator = LastTimeUsedComparator()

    companion object {
        private const val ALL_APP_PACKAGE = "io.sunshine0523.sidebar"
        private const val ALL_APP_ACTIVITY = "io.sunshine0523.sidebar.ui.all_app.AllAppActivity"
    }

    init {
        initSidebarAppList()
        initAppListChangeCallback()
    }

    private fun initSidebarAppList() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllSidebarAppsByFlow().collect { sidebarAppList ->
                if (Debug.isDebug) logger.d("$sidebarAppList")
                _sidebarAppList.value.clear()
                _sidebarAppList.value.add(
                    AppInfo(
                        "",
                        AppCompatResources.getDrawable(application.applicationContext, R.drawable.ic_all)!!,
                        ALL_APP_PACKAGE,
                        ALL_APP_ACTIVITY,
                        0
                    )
                )
                sidebarAppList?.forEach { entity ->
                    runCatching {
                        val info = application.packageManager.getApplicationInfo(entity.packageName, PackageManager.GET_ACTIVITIES)
                        _sidebarAppList.value.add(
                            AppInfo(
                                "${info.loadLabel(application.packageManager)}",
                                info.loadIcon(application.packageManager),
                                entity.packageName,
                                entity.activityName,
                                entity.userId
                            )
                        )
                    }.onFailure {
                        repository.deleteSidebarApp(entity.packageName, entity.activityName, entity.userId)
                    }
                }
            }
        }
    }

    suspend fun getRecentAppListFlow(): MutableStateFlow<ArrayList<AppInfo>> {
        val recentListFlow = MutableStateFlow<ArrayList<AppInfo>>(ArrayList())
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - 1000 * 60 * 60
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            currentTime
        )
        if (Debug.isDebug) logger.d("${usageStatsList}")
        val recentList = ArrayList<AppInfo>()
        // 根据上次打开时间进行排序
        Collections.sort(usageStatsList, lastTimeUsedComparator)
        usageStatsList.forEach { usageStats ->
            runCatching {
                val info = application.packageManager.getApplicationInfo(usageStats.packageName, PackageManager.GET_ACTIVITIES)
                val launchIntent = application.packageManager.getLaunchIntentForPackage(usageStats.packageName)
                if (launchIntent != null && launchIntent.component != null) {
                    val appInfo = AppInfo(
                        "${info.loadLabel(application.packageManager)}",
                        info.loadIcon(application.packageManager),
                        info.packageName,
                        launchIntent.component!!.className,
                        0
                    )
                    if (_sidebarAppList.value.contains(appInfo).not()) {
                        recentList.add(
                            appInfo
                        )
                    }
                }
            }.onFailure {
                if (Debug.isDebug) logger.d("getRecentAppListFlow $it")
            }
        }
        recentListFlow.emit(recentList)
        return recentListFlow
    }

    fun getIntSp(name: String, defaultValue: Int): Int {
        return sp.getInt(name, defaultValue)
    }

    fun getBooleanSp(name: String, defaultValue: Boolean): Boolean {
        return sp.getBoolean(name, defaultValue)
    }

    fun setIntSp(name: String, value: Int) {
        sp.edit().apply {
            putInt(name, value)
            apply()
        }
    }

    fun registerSpChangeListener(listener: OnSharedPreferenceChangeListener) {
        sp.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterSpChangeListener(listener: OnSharedPreferenceChangeListener) {
        sp.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun initAppListChangeCallback() {
        launcherApps.registerCallback(object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                val sidebarInfo = _sidebarAppList.value.getInfo(packageName, user)
                if (sidebarInfo != null) {
                    repository.deleteSidebarApp(sidebarInfo.packageName, sidebarInfo.activityName, sidebarInfo.userId)
                }
                _sidebarAppList.value.remove(sidebarInfo)
                //_appList.value.remove(_appList.value.getInfo(packageName, user))
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                //_appList.value.remove(_appList.value.getInfo(packageName, user))

                runCatching {
                    val info = application.packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
                    val launchIntent = application.packageManager.getLaunchIntentForPackage(packageName)
                    val userId = UserHandleHidden.getUserId(user)
//                    if (launchIntent != null && launchIntent.component != null) {
//                        _appList.value.add(
//                            AppInfo(
//                                "${info.loadLabel(application.packageManager)}${if (userId != 0) -userId else ""}",
//                                info.loadIcon(application.packageManager),
//                                info.packageName,
//                                launchIntent.component!!.className,
//                                userId
//                            )
//                        )
//                    }
//                    Collections.sort(allAppList, appComparator)
                }
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {

            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean
            ) {

            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean
            ) {

            }

        })
    }

    private inner class LastTimeUsedComparator : Comparator<UsageStats> {
        override fun compare(a: UsageStats, b: UsageStats): Int {
            return (b.lastTimeUsed - a.lastTimeUsed).toInt()
        }
    }
}

