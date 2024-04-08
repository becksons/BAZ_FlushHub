package com.example.flushhubproto

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeGestureDetector(private val onSwipeUp: () -> Unit) : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val deltaY = e2.y - e1!!.y
        val deltaX = e2.x - e1.x

        if (abs(deltaY) > abs(deltaX) && deltaY < 0) {
            onSwipeUp()
            return true
        }
        return false
    }
}
