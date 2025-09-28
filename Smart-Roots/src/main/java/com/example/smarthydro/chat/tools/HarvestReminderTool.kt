package com.example.smarthydro.chat.tools

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class HarvestReminderTool(private val appContext: Context) : Tool {

    override fun matches(userText: String): Boolean =
        listOf("remind", "harvest", "reminder").any { userText.contains(it, ignoreCase = true) }

    override suspend fun invoke(userText: String): ToolResult {
        // Very simple parse: "remind harvest in X days" or "remind harvest in X hours"
        val lower = userText.lowercase()
        val days  = Regex("""in\s+(\d+)\s*days?""").find(lower)?.groupValues?.getOrNull(1)?.toLongOrNull()
        val hours = Regex("""in\s+(\d+)\s*hours?""").find(lower)?.groupValues?.getOrNull(1)?.toLongOrNull()

        val delayMinutes = when {
            days != null  -> max(1, days * 24 * 60)
            hours != null -> max(1, hours * 60)
            else -> 60L // default 1 hour
        }

        val title = "Harvest reminder"
        val body  = "It's time to check if plants are ready to harvest."
        scheduleWindowedHarvestReminder(appContext, delayMinutes, title, body)

        val pretty = when {
            days != null  -> "$days day(s)"
            hours != null -> "$hours hour(s)"
            else          -> "~1 hour"
        }
        return ToolResult("⏰ Okay! I’ll remind you in $pretty.")
    }

    private fun scheduleWindowedHarvestReminder(
        ctx: Context,
        delayMinutes: Long,
        title: String,
        body: String
    ) {
        val req = OneTimeWorkRequestBuilder<HarvestWork>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf("title" to title, "body" to body))
            .build()
        WorkManager.getInstance(ctx).enqueue(req)
    }
}
