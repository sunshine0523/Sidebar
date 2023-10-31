package io.sunshine0523.sidebar.ui.all_app

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import android.os.UserManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.systemapi.UserHandleHidden
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger
import io.sunshine0523.sidebar.utils.getInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Collections
import java.util.Locale

/**
 * @author KindBrave
 * @since 2023/10/25
 */
class AllAppViewModel(private val application: Application): AndroidViewModel(application) {
    private val logger = Logger("AllAppViewModel")
    private val allAppList = ArrayList<AppInfo>()
    val appListFlow: Flow<List<AppInfo>>
        get() = _appList
    private val _appList = MutableSharedFlow<ArrayList<AppInfo>>()

    private val userManager: UserManager = application.getSystemService(Context.USER_SERVICE) as UserManager
    private val launcherApps: LauncherApps = application.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    private val appComparator = AppComparator()

    init {
        initAllAppList()
        initAppListChangeCallback()
    }

    private fun initAllAppList() {
        val userHandleMap = HashMap<Int, UserHandle>()
        userManager.userProfiles.forEach {
            userHandleMap[UserHandleHidden.getUserId(it)] = it
        }
        viewModelScope.launch(Dispatchers.IO) {
            userManager.userProfiles.forEach { userHandle ->
                val list = launcherApps.getActivityList(null, userHandle)
                list.forEach {info ->
                    val userId = UserHandleHidden.getUserId(userHandle)
                    allAppList.add(
                        AppInfo(
                            "${info.label}${if (userId != 0) -userId else ""}",
                            info.applicationInfo.loadIcon(application.packageManager),
                            info.componentName.packageName,
                            info.componentName.className,
                            userId
                        )
                    )
                }
            }
            Collections.sort(allAppList, appComparator)
            _appList.emit(allAppList)
        }
    }

    private fun initAppListChangeCallback() {
        launcherApps.registerCallback(object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                if (Debug.isDebug) logger.d("onPackageRemoved $packageName $user")
                viewModelScope.launch(Dispatchers.IO) {
                    allAppList.remove(allAppList.getInfo(packageName, user))
                    _appList.emit(allAppList)
                }
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                if (Debug.isDebug) logger.d("onPackageAdded $packageName $user")
                viewModelScope.launch(Dispatchers.IO) {
                    allAppList.remove(allAppList.getInfo(packageName, user))
                }

                runCatching {
                    val info = application.packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
                    val launchIntent = application.packageManager.getLaunchIntentForPackage(packageName)
                    val userId = UserHandleHidden.getUserId(user)
                    if (launchIntent != null && launchIntent.component != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            allAppList.add(
                                AppInfo(
                                    "${info.loadLabel(application.packageManager)}${if (userId != 0) -userId else ""}",
                                    info.loadIcon(application.packageManager),
                                    info.packageName,
                                    launchIntent.component!!.className,
                                    userId
                                )
                            )
                            Collections.sort(allAppList, appComparator)
                            _appList.emit(allAppList)
                        }
                    }
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

    fun filterApp(filter: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (filter.isBlank()) {
                _appList.emit(allAppList)
            }
            else {
                val filterAppList = allAppList.filter { appInfo ->
                    appInfo.label.contains(filter, true)
                }
                Collections.sort(filterAppList, appComparator)
                _appList.emit(filterAppList as ArrayList<AppInfo>)
            }
        }
    }

    private inner class AppComparator : Comparator<AppInfo> {
        override fun compare(p0: AppInfo, p1: AppInfo): Int {
            return Collator.getInstance(Locale.CHINESE).compare(p0.label, p1.label)
        }
    }
}