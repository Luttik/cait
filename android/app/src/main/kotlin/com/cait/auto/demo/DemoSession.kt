package com.cait.auto.demo

import android.content.Intent
import androidx.car.app.Session
import androidx.car.app.Screen

class DemoSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = DemoScreen(carContext)
}
