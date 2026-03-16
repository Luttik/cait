package com.cait.auto

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Android Auto entry point for the Cait application.
 *
 * The system binds to this service when the app is launched on the
 * car's head unit.  It creates a [CaitSession] which in turn pushes
 * [CaitScreen] as the initial screen.
 */
class CaitCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = CaitSession()
}

private class CaitSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen =
        CaitScreen(carContext)
}
