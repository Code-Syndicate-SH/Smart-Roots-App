package com.example.smarthydro.chat.tools

class HarvestAdviceTool : Tool {
    override fun matches(t: String) =
        listOf("harvest tips", "ready to harvest", "ripeness").any { t.contains(it, true) }

    override suspend fun invoke(t: String) = ToolResult(
        """
        General harvest tips:
        • Leafy greens: 30–45 days after sowing; pick outer leaves first.
        • Basil: pinch above a node; avoid flowering for better flavor.
        • Tomatoes: harvest when fully colored and slightly soft.
        • Peppers: green = crisp; full color = sweeter.
        """.trimIndent()
    )
}
