package com.cait.auto

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template

/**
 * Approval screen shown when the agent wants to execute a tool.
 *
 * Presents the tool call details to the driver with Approve / Reject
 * buttons.  The result is delivered back via the [onResult] callback
 * and the screen is popped from the stack.
 */
class ApprovalScreen(
    carContext: CarContext,
    private val toolCallData: String,
    private val onResult: (Boolean) -> Unit
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val displayText = "The assistant wants to perform an action:\n\n$toolCallData"

        return MessageTemplate.Builder(displayText)
            .setHeader(
                Header.Builder()
                    .setTitle(carContext.getString(R.string.approve_title))
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.approve_button))
                    .setOnClickListener {
                        onResult(true)
                        screenManager.pop()
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.reject_button))
                    .setOnClickListener {
                        onResult(false)
                        screenManager.pop()
                    }
                    .build()
            )
            .build()
    }
}
