package io.sunshine0523.sidebar.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import io.sunshine0523.sidebar.app.SidebarApplication
import io.sunshine0523.sidebar.bean.AppInfo
import io.sunshine0523.sidebar.databinding.ViewSidebarBinding
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * @author KindBrave
 * @since 2023/9/26
 */
class SidebarView(
    private val context: Context,
    private val viewModel: ServiceViewModel,
    private val callback: Callback
) : SidebarRecyclerAdapter.Callback {

    private val dataBinding: ViewSidebarBinding = ViewSidebarBinding.inflate(LayoutInflater.from(context))
    private var sidebarPositionX = 0
    private var sidebarPositionY = 0
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutParams = LayoutParams()
    private val scope = MainScope()
    private val logger = Logger(TAG)

    companion object {
        private const val SIDELINE_POSITION_X = "sideline_position_x"
        private const val OFFSET_X = 90
        private const val PACKAGE = "com.sunshine.freeform"
        private const val ACTION = "com.sunshine.freeform.start_freeform"
        private const val TAG = "SidebarView"
    }

    override fun onClick(appInfo: AppInfo) {
        val intent = Intent(ACTION).apply {
            setPackage(PACKAGE)
            putExtra("packageName", appInfo.packageName)
            putExtra("activityName", appInfo.activityName)
            putExtra("userId", appInfo.userId)
        }
        context.sendBroadcast(intent)
        removeView()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showView() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        val sidebarHeight = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenHeight / 3
        } else {
            (screenHeight * 0.8f).roundToInt()
        }

        sidebarPositionX = viewModel.getIntSp(SIDELINE_POSITION_X, -1)
        sidebarPositionY = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            -screenHeight / 6
        } else {
            0
        }

        initRecyclerView()

        layoutParams.apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            width = LayoutParams.WRAP_CONTENT
            height = sidebarHeight
            x = sidebarPositionX * (screenWidth / 2 - OFFSET_X)
            y = sidebarPositionY
            flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    LayoutParams.FLAG_HARDWARE_ACCELERATED
            format = PixelFormat.RGBA_8888
            windowAnimations = android.R.style.Animation_Dialog
        }

        dataBinding.root.translationX = sidebarPositionX * 1.0f * 200

        dataBinding.root.setOnTouchListener { view, event ->
            if (Debug.isDebug) logger.d("$view $event")
            if (event.action == MotionEvent.ACTION_UP) {
                removeView()
                true
            }
            false
        }

        runCatching {
            windowManager.addView(dataBinding.root, layoutParams)
            dataBinding.root.animate().translationX(0f).setDuration(300).start()
        }
    }

    fun removeView() {
        runCatching { windowManager.removeViewImmediate(dataBinding.root) }
        callback.onRemove()
    }

    private fun initRecyclerView() {
        val adapter = SidebarRecyclerAdapter(this@SidebarView)
        dataBinding.recyclerView.adapter = adapter
        dataBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        scope.launch(Dispatchers.IO) {
            viewModel.sidebarAppListFlow.collect {
                launch(Dispatchers.Main) {
                    if (Debug.isDebug) logger.d("updateSidebarAppList $it")
                    adapter.updateSidebarAppList(it)
                }
            }
        }
        scope.launch(Dispatchers.IO) {
            viewModel.getRecentAppListFlow().collect {
                launch(Dispatchers.Main) {
                    if (Debug.isDebug) logger.d("updateRecentAppList $it")
                    adapter.updateRecentAppList(it)
                }
            }
        }
    }

    interface Callback {
        fun onRemove()
    }
}