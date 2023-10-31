package io.sunshine0523.sidebar.ui.sidebar

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.bean.SidebarAppInfo
import io.sunshine0523.sidebar.room.DatabaseRepository
import io.sunshine0523.sidebar.room.SidebarAppsEntity
import io.sunshine0523.sidebar.systemapi.UserHandleHidden
import io.sunshine0523.sidebar.utils.contains
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
 * @since 2023/10/21
 */
class SidebarAppSettingViewModel(private val application: Application) : AndroidViewModel(application) {
    private val repository = DatabaseRepository(application)
    private val allAppList = ArrayList<SidebarAppInfo>()
    val appListFlow: Flow<List<SidebarAppInfo>>
        get() = _appList
    private val _appList = MutableSharedFlow<ArrayList<SidebarAppInfo>>()

    private val launcherApps: LauncherApps = application.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager: UserManager = application.getSystemService(Context.USER_SERVICE) as UserManager

    private val appComparator = AppComparator()

    init {
        initAllAppList()
    }

    fun addSidebarApp(packageName: String, activityName: String, userId: Int) {
        repository.insertSidebarApp(packageName, activityName, userId)
    }

    fun deleteSidebarApp(packageName: String, activityName: String, userId: Int) {
        repository.deleteSidebarApp(packageName, activityName, userId)
    }

    private fun initAllAppList() {
        val userHandleMap = HashMap<Int, UserHandle>()
        userManager.userProfiles.forEach {
            userHandleMap[UserHandleHidden.getUserId(it)] = it
        }
        viewModelScope.launch(Dispatchers.IO) {
            val sidebarAppList = repository.getAllSidebarWithoutLiveData()
            userManager.userProfiles.forEach { userHandle ->
                val list = launcherApps.getActivityList(null, userHandle)
                list.forEach {info ->
                    val userId = UserHandleHidden.getUserId(userHandle)
                    allAppList.add(
                        SidebarAppInfo(
                            "${info.label}${if (userId != 0) -userId else ""}",
                            info.applicationInfo,
                            info.componentName.packageName,
                            info.componentName.className,
                            userId,
                            sidebarAppList?.contains(info.componentName.packageName, info.componentName.className, userId) ?: false
                        )
                    )
                }
            }
            Collections.sort(allAppList, appComparator)
            _appList.emit(allAppList)
        }
    }

    private inner class AppComparator : Comparator<SidebarAppInfo> {
        override fun compare(p0: SidebarAppInfo, p1: SidebarAppInfo): Int {
            return Collator.getInstance(Locale.CHINESE).compare(p0.label, p1.label)
        }
    }
}