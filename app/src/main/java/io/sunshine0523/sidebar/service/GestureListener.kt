package io.sunshine0523.sidebar.service

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import io.sunshine0523.sidebar.utils.Debug
import io.sunshine0523.sidebar.utils.Logger

/**
 * @author KindBrave
 * @since 2023/9/27
 */
class GestureListener(private val callback: Callback) : MGestureManager.MGestureListener {
    private val logger = Logger(TAG)
    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f
    private var isLongPress = false
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable {
        isLongPress = true
        callback.beginMoveSideline()
    }

    companion object {
        private const val TAG = "GestureListener"
    }

    override fun singleFingerSlipAction(
        gestureEvent: MGestureManager.GestureEvent?,
        startEvent: MotionEvent?,
        endEvent: MotionEvent?,
        velocity: Float
    ): Boolean {
        if (null != gestureEvent) {
            if (gestureEvent == MGestureManager.GestureEvent.SINGLE_GINGER_LEFT_SLIP || gestureEvent == MGestureManager.GestureEvent.SINGLE_GINGER_RIGHT_SLIP) {
                callback.showSidebar()
            }
            return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY

                isLongPress = false
                longPressHandler.postDelayed(longPressRunnable, 500)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLongPress) {
                    if (Debug.isDebug) logger.d("${event.rawX}")
                    callback.moveSideline((event.rawX - initialTouchX).toInt(), (event.rawY - initialTouchY).toInt(), event.rawX.toInt(), event.rawY.toInt())
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                }
            }
            MotionEvent.ACTION_UP -> {
                longPressHandler.removeCallbacks(longPressRunnable)
                isLongPress = false
                callback.endMoveSideline()
            }
        }
    }

    interface Callback {
        fun showSidebar()
        fun beginMoveSideline()
        fun moveSideline(xChanged: Int, yChanged: Int, touchX: Int, touchY: Int)
        fun endMoveSideline()
    }
}