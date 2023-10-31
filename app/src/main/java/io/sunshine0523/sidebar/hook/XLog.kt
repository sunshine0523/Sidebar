package io.sunshine0523.sidebar.hook

import de.robv.android.xposed.XposedBridge

object XLog {

    fun i(s: Any) {
        XposedBridge.log("[Sidebar/I] $s")
    }

    fun d(s: Any) {
        XposedBridge.log("[Sidebar/D] $s")
    }

    fun e(s: Any) {
        XposedBridge.log("[Sidebar/E] $s")
    }
}