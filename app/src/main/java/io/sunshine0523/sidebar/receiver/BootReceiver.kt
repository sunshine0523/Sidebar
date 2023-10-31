package io.sunshine0523.sidebar.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.sunshine0523.sidebar.service.SidebarService
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger
import java.util.logging.Handler

/**
 * @author KindBrave
 * @since 2023/9/19
 */
class BootReceiver : BroadcastReceiver() {
    private val logger = Logger(TAG)
    companion object {
        private const val BOOT = "android.intent.action.BOOT_COMPLETED"
        private const val TAG = "BootReceiver"
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BOOT) {
            if (Debug.isDebug) logger.d("Boot Completed")
            context.startService(Intent(context, SidebarService::class.java))
        }
    }
}