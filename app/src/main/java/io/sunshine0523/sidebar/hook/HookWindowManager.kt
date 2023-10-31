package io.sunshine0523.sidebar.hook

import android.os.Binder
import android.os.Build
import android.system.Os
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowManager
import android.view.WindowManagerGlobal
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author KindBrave
 * @since 2023/9/25
 */
class HookWindowManager : IXposedHookLoadPackage {

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName == "android") {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) hookWMSBeforeR(param.classLoader)
            else hookWMS(param.classLoader)
        }
    }

    private fun hookWMSBeforeR(classLoader: ClassLoader) {
        val wms = XposedHelpers.findClass("com.android.server.wm.WindowManagerService", classLoader)
        XposedBridge.hookAllMethods(
            wms,
            "addWindow",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val layoutParams = param.args[3] as WindowManager.LayoutParams
                    if (layoutParams.packageName == "io.sunshine0523.sidebar" && layoutParams.type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
                        val newToken =
                            Binder.clearCallingIdentity() and 0xFFFFFFFFL or (1000L shl 32)
                        Binder.restoreCallingIdentity(newToken)
                        XLog.i("addWindow successful")
                    }
                }
            }
        )
    }

    private fun hookWMS(classLoader: ClassLoader) {
        val wms = XposedHelpers.findClass("com.android.server.wm.WindowManagerService", classLoader)
        XposedBridge.hookAllMethods(
            wms,
            "addWindow",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val layoutParams = param.args[2] as WindowManager.LayoutParams
                    if (layoutParams.packageName == "io.sunshine0523.sidebar" && layoutParams.type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
                        val newToken =
                            Binder.clearCallingIdentity() and 0xFFFFFFFFL or (1000L shl 32)
                        Binder.restoreCallingIdentity(newToken)
                        XLog.i("addWindow successful")
                    }
                }
            }
        )
    }
}