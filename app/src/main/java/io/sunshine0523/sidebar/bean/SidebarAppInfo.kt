package io.sunshine0523.sidebar.bean

import android.content.pm.ApplicationInfo

data class SidebarAppInfo(
    val label: String,
    val applicationInfo: ApplicationInfo,
    val packageName: String,
    val activityName: String,
    var userId: Int,
    // 当前APP是否在侧边栏中展示
    var isSidebarApp: Boolean = false
)
