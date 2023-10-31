package io.sunshine0523.sidebar.utils

import android.os.UserHandle
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.room.SidebarAppsEntity

/**
 * @author KindBrave
 * @since 2023/8/29
 */
fun <T> List<T>.contains(element: T, predicate: (T, T) -> Boolean): Boolean {
    for (item in this) {
        if (predicate(item, element)) {
            return true
        }
    }
    return false
}

fun ArrayList<AppInfo>.getInfo(packageName: String, userHandle: UserHandle): AppInfo? {
    for (item in this) {
        if (
            item.packageName == packageName &&
            item.userId == io.sunshine0523.sidebar.systemapi.UserHandleHidden.getUserId(userHandle)) return item
    }
    return null
}

/**
 * @author KindBrave
 * @since 2023/10/21
 * 判断侧边栏数据库列表中是否包含该包名、活动名、userId的内容
 */
fun List<SidebarAppsEntity>.contains(packageName: String, activityName: String, userId: Int): Boolean {
    for (item in this) {
        if (
            item.packageName == packageName &&
            item.activityName == activityName &&
            item.userId == userId
        ) return true
    }
    return false
}