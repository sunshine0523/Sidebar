package io.sunshine0523.sidebar.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.view.WindowManagerHidden
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import io.sunshine0523.sidebar.R
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger

class SidebarService : Service(), SharedPreferences.OnSharedPreferenceChangeListener,
    GestureListener.Callback {
    private val logger = Logger(TAG)
    private lateinit var viewModel: ServiceViewModel
    private lateinit var windowManager: WindowManager
    private lateinit var sideLineView: View
    private lateinit var sidebarView: SidebarView
    private var showSideline = false
    private var showSidebar = false
    private var sidelinePositionX = 0
    private var sidelinePositionY = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private val layoutParams = WindowManagerHidden.LayoutParams()

    companion object {
        private const val TAG = "SidebarService"
        private const val SIDELINE_WIDTH = 100
        //侧边条移动时的宽度
        private const val SIDELINE_MOVE_WIDTH = 200
        private const val SIDELINE_HEIGHT = 200
        //侧边条屏幕边缘偏移量
        private const val OFFSET = 20

        //是否展示侧边条
        private const val SIDELINE = "sideline"
        private const val SIDELINE_POSITION_X = "sideline_position_x"
        private const val SIDELINE_POSITION_Y_PORTRAIT = "sideline_position_y_portrait"
        private const val SIDELINE_POSITION_Y_LANDSCAPE = "sideline_position_y_landscape"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        viewModel = ServiceViewModel(application)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels
        viewModel.registerSpChangeListener(this)
        sidebarView = SidebarView(this@SidebarService, viewModel, object : SidebarView.Callback {
            override fun onRemove() {
                if (showSidebar) runCatching { showSideline() }
                showSidebar = false
            }
        })
        showSideline = viewModel.getBooleanSp(SIDELINE, true)
        if (showSideline) showView()
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels
        showView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized) viewModel.unregisterSpChangeListener(this)
        removeView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            SIDELINE -> {
                showSideline = viewModel.getBooleanSp(SIDELINE, true)
                if (showSideline) {
                    showView()
                } else {
                    removeView()
                }
            }
            SIDELINE_POSITION_X -> {
                sidelinePositionX = viewModel.getIntSp(SIDELINE_POSITION_X, -1)
                updateView()
            }
            SIDELINE_POSITION_Y_PORTRAIT -> {
                sidelinePositionY = viewModel.getIntSp(SIDELINE_POSITION_Y_PORTRAIT, -screenHeight / 6)
                updateView()
            }
            SIDELINE_POSITION_Y_LANDSCAPE -> {
                sidelinePositionY = viewModel.getIntSp(SIDELINE_POSITION_Y_LANDSCAPE, -screenHeight / 6)
                updateView()
            }
        }
    }

    override fun showSidebar() {
        sidebarView.showView()
        showSidebar = true
        hideSideline()
    }

    override fun beginMoveSideline() {
        layoutParams.apply {
            width = SIDELINE_MOVE_WIDTH
        }
        runCatching { windowManager.updateViewLayout(sideLineView, layoutParams) }
    }

    /**
     * @param xChanged x轴变化
     * @param yChanged y轴变化
     * @param positionX 触摸的x轴绝对位置。用来判断是否需要变化侧边条展示位置
     * @param positionY 触摸的y轴绝对位置
     */
    override fun moveSideline(xChanged: Int, yChanged: Int, positionX: Int, positionY: Int) {
        sidelinePositionX = if (positionX > screenWidth / 2) 1 else -1
        layoutParams.apply {
            x = sidelinePositionX * (screenWidth / 2 - OFFSET)
            y = layoutParams.y + yChanged
        }
        runCatching { windowManager.updateViewLayout(sideLineView, layoutParams) }
    }

    override fun endMoveSideline() {
        layoutParams.apply {
            width = SIDELINE_WIDTH
        }
        runCatching { windowManager.updateViewLayout(sideLineView, layoutParams) }
        viewModel.setIntSp(SIDELINE_POSITION_X, sidelinePositionX)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            viewModel.setIntSp(SIDELINE_POSITION_Y_PORTRAIT, layoutParams.y)
        } else {
            viewModel.setIntSp(SIDELINE_POSITION_Y_LANDSCAPE, layoutParams.y)
        }
    }

    /**
     * 启动侧边条
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun showView() {
        removeView()
        sidelinePositionX = viewModel.getIntSp(SIDELINE_POSITION_X, -1)
        sidelinePositionY =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                viewModel.getIntSp(SIDELINE_POSITION_Y_PORTRAIT, -screenHeight / 6)
            else
                viewModel.getIntSp(SIDELINE_POSITION_Y_LANDSCAPE, -screenHeight / 6)
        sideLineView = View(this)
        sideLineView.background = AppCompatResources.getDrawable(this, R.drawable.ic_line)
        runCatching {
            layoutParams.apply {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                width = SIDELINE_WIDTH
                height = SIDELINE_HEIGHT
                x = sidelinePositionX * (screenWidth / 2 - OFFSET)
                y = sidelinePositionY
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                privateFlags = WindowManagerHidden.LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS or WindowManagerHidden.LayoutParams.PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY or WindowManagerHidden.LayoutParams.PRIVATE_FLAG_USE_BLAST or WindowManagerHidden.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY
                format = PixelFormat.RGBA_8888
                windowAnimations = android.R.style.Animation_Dialog
            }
            val gestureManager = MGestureManager(this@SidebarService, GestureListener(this@SidebarService))
            sideLineView.setOnTouchListener { _, event ->
                gestureManager.onTouchEvent(event)
                true
            }
            windowManager.addView(sideLineView, layoutParams)
        }.onFailure {
            if (Debug.isDebug) logger.d("$it")
            Toast.makeText(this, getString(R.string.check_xposed_permission), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateView() {
        runCatching {
            layoutParams.apply {
                x = sidelinePositionX * (screenWidth / 2 - OFFSET)
                y = sidelinePositionY
            }
            windowManager.updateViewLayout(sideLineView, layoutParams)
        }
    }

    private fun removeView() {
        runCatching { windowManager.removeViewImmediate(sideLineView) }
        runCatching { sidebarView.removeView() }
    }

    private fun hideSideline() {
        sideLineView.animate().translationX(sidelinePositionX * 1.0f * SIDELINE_WIDTH).setDuration(300).start()
    }

    private fun showSideline() {
        sideLineView.animate().translationX(0f).setDuration(300).start()
    }
}