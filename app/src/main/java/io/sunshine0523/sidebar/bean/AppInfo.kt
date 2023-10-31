package io.sunshine0523.sidebar.bean

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val icon: Drawable,
    val packageName: String,
    val activityName: String,
    val userId: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other is AppInfo) {
            if (packageName == other.packageName && activityName == other.activityName) return true
            return false
        }
        return false
    }
}
