package io.sunshine0523.sidebar.app

import android.app.Application
import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass

/**
 * @author KindBrave
 * @since 2023/9/25
 */
class SidebarApplication : Application() {
    companion object {
        const val CONFIG = "config"
        init {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }
}