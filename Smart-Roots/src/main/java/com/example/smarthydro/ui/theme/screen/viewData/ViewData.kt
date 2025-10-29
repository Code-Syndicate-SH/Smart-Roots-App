package com.example.smarthydro.ui.theme.screen.viewData

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthydro.R
import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.viewmodels.ComponentViewModel
import com.example.smarthydro.viewmodels.ReadingViewModel
import com.example.smarthydro.viewmodels.SensorViewModel

private data class SensorSpec(
    val key: String,
    @DrawableRes val iconResId: Int,
    val label: String,
    val unit: String,
    val idealRange: ClosedFloatingPointRange<Float>? = null,
    val controlType: ControlType = ControlType.TOGGLE
)

private enum class ControlType {
    TOGGLE,
    ADJUSTABLE,
    NONE
}

private val DangerRed = Color(0xFFD32F2F)
private val WarningOrange = Color(0xFFFFA000)
private val IdealBlue = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    navHostController: NavHostController,
    componentViewModel: ComponentViewModel,
    readingViewModel: ReadingViewModel,
    sensorViewModel: SensorViewModel
) {
    val context = LocalContext.current

    val sensorData by sensorViewModel.sensorData.observeAsState(SensorModel())
    val readingType = readingViewModel.getReadingType() ?: return

    val spec = remember(readingType.heading) {
        getSensorSpec(context, readingType.heading)
    }

    val series = remember { mutableStateListOf<Float>() }

    LaunchedEffect(sensorData) {
        val currentValue = getCurrentValue(spec.key, sensorData)
        if (currentValue != null) {
            series.add(currentValue)
            if (series.size > 30) {
                series.removeAt(0)
            }
        }
    }

    val (statusText, statusColor) = remember(sensorData) {
        val value = getCurrentValue(spec.key, sensorData)
        determineStatus(value, spec.idealRange)
    }
    val macAddress:String? = readingType.macAddress
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spec.label) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.navigate("home") }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DeepBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ValueHeader(
                iconResId = spec.iconResId,
                label = spec.label,
                valueText = getCurrentValue(spec.key, sensorData)?.format(1) ?: "--",
                unit = spec.unit,
                valueColor = statusColor,
                statusChip = { StatusPill(statusText, statusColor) }
            )

            ControlPanel(
                spec = spec,
                componentViewModel = componentViewModel,
                macAddress = macAddress
            )

            SRSectionCard(
                title = "Recent Readings",
                subtitle = "Live data from the last 30 updates"
            ) {
                Sparkline(values = series)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoStat("Min", "${series.minOrNull()?.format(1) ?: "--"} ${spec.unit}")
                    InfoStat("Max", "${series.maxOrNull()?.format(1) ?: "--"} ${spec.unit}")
                    spec.idealRange?.let { ideal ->
                        InfoStat("Ideal", "${ideal.start.format(1)}–${ideal.endInclusive.format(1)} ${spec.unit}")
                    }
                }
            }

            TipsCard(
                lines = getTipsForSensor(spec.key, statusText)
            )
        }
    }
}

@Composable
private fun ControlPanel(spec: SensorSpec, componentViewModel: ComponentViewModel, macAddress:String?=null) {
    val openAlertDialogLow = remember { mutableStateOf(false) }
    val openAlertDialogUp = remember { mutableStateOf(false) }
    var switchState by remember { mutableStateOf(false) }

    when (spec.controlType) {
        ControlType.TOGGLE -> {
            BigTile(
                title = "Device Control",
                subtitle = "Toggle the ${spec.label.lowercase()} system"
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (switchState) "ON" else "OFF", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 16.dp), color = Color.White)
                    PillSwitch(
                        checked = switchState,
                        onCheckedChange = {
                            switchState = it
                            when (spec.key) {
                                "temperature" -> componentViewModel.setFan(macAddress)
                                "light" -> componentViewModel.setLight(macAddress)
                                "humidity" -> componentViewModel.setExtractor(macAddress)
                                "water" -> componentViewModel.setPump(macAddress)
                            }
                        }
                    )
                }
            }
        }
        ControlType.ADJUSTABLE -> {
            SRSectionCard(
                title = "Solution Adjustment",
                subtitle = "Manually dose the reservoir to adjust levels"
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { openAlertDialogUp.value = true }) {
                        Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Increase", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Increase")
                    }
                    Button(
                        onClick = { openAlertDialogLow.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Decrease", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Decrease")
                    }
                }
            }

            if (openAlertDialogUp.value) {
                AlertDialogModel(
                    onDismissRequest = { openAlertDialogUp.value = false },
                    onConfirmation = {
                        if (spec.key == "ph") componentViewModel.setPhUp(macAddress) else componentViewModel.setEcUp(macAddress)
                        openAlertDialogUp.value = false
                    },
                    dialogTitle = "Increase Solution",
                    dialogText = "This will dispense the 'UP' solution. Are you sure?",
                    icon = Icons.Default.Info
                )
            }
            if (openAlertDialogLow.value) {
                AlertDialogModel(
                    onDismissRequest = { openAlertDialogLow.value = false },
                    onConfirmation = {
                        if (spec.key == "ph") componentViewModel.setPhDown() else componentViewModel.setEcDown()
                        openAlertDialogLow.value = false
                    },
                    dialogTitle = "Decrease Solution",
                    dialogText = "This will dispense the 'DOWN' solution. Are you sure?",
                    icon = Icons.Default.Info
                )
            }
        }
        ControlType.NONE -> {
            // No controls to show
        }
    }
}

private fun getSensorSpec(context: Context, heading: String): SensorSpec {
    return when (heading) {
        context.getString(R.string.Temperature) -> SensorSpec("temperature", R.drawable.mode_fan_24px, "Temperature", "°C", 20f..28f)
        context.getString(R.string.Water_Flow) -> SensorSpec("water", R.drawable.ic_waterlv, "Water Flow", "L/hr", controlType = ControlType.TOGGLE)
        context.getString(R.string.Water_pH) -> SensorSpec("ph", R.drawable.ic_cleanwater, "Water pH", "pH", 5.8f..6.5f, ControlType.ADJUSTABLE)
        context.getString(R.string.Humidity) -> SensorSpec("humidity", R.drawable.heat_pump_24px, "Humidity", "%", 55f..75f)
        context.getString(R.string.EC_Reading) -> SensorSpec("ec", R.drawable.ic_plant, "Conductivity", "mS/cm", 0.8f..2.0f, ControlType.ADJUSTABLE)
        context.getString(R.string.Light) -> SensorSpec("light", R.drawable.ic_light, "Light", "lumens", controlType = ControlType.TOGGLE)
        else -> SensorSpec("unknown", R.drawable.ic_plant, "Unknown", "", controlType = ControlType.NONE)
    }
}

private fun String.toFloatOrNullSanitized(): Float? {
    val sanitized = this.replace(Regex("[^0-9.]"), "")
    return sanitized.toFloatOrNull()
}

private fun getCurrentValue(key: String, data: SensorModel): Float? {
    return when (key) {
        "temperature" -> data.temperature.toFloatOrNullSanitized()
        "water"       -> data.flowRate.toFloatOrNullSanitized()
        "ph"          -> data.pH.toFloatOrNullSanitized()
        "humidity"    -> data.humidity.toFloatOrNullSanitized()
        "ec"          -> data.eC.toFloatOrNullSanitized()
        "light"       -> data.light.toFloatOrNullSanitized()
        else          -> null
    }
}

private fun determineStatus(value: Float?, idealRange: ClosedFloatingPointRange<Float>?): Pair<String, Color> {
    if (value == null || idealRange == null) {
        return "N/A" to Color.Gray
    }
    return when {
        value < idealRange.start -> "Low" to WarningOrange
        value > idealRange.endInclusive -> "High" to DangerRed
        else -> "Ideal" to IdealBlue
    }
}

private fun getTipsForSensor(key: String, status: String): List<String> {
    return when (key) {
        "temperature" -> when (status) {
            "High" -> listOf("High temp detected. Increase fan speed or check for external heat sources.", "Ensure ventilation is unobstructed.")
            "Low" -> listOf("Low temp detected. Reduce fan speed or drafts, especially during the light-off period.")
            else -> listOf("Temperature is in the ideal range for healthy growth.")
        }
        "ph" -> when (status) {
            "High" -> listOf("pH is too high (alkaline). Use 'pH Down' solution in small amounts.", "High pH can lock out key nutrients like iron.")
            "Low" -> listOf("pH is too low (acidic). Use 'pH Up' solution.", "Ensure your pH sensor is calibrated correctly.")
            else -> listOf("pH is stable. This allows for optimal nutrient absorption by the roots.")
        }
        "ec" -> when (status) {
            "High" -> listOf("EC is high. This may indicate over-feeding. Dilute the reservoir with fresh water.", "High EC can burn plant roots.")
            "Low" -> listOf("EC is low. Your plants may need more nutrients. Increase concentration gradually.")
            else -> listOf("Nutrient concentration is ideal for the current growth stage.")
        }
        "humidity" -> when (status) {
            "High" -> listOf("High humidity increases mold risk. Increase air circulation with the fan.", "Ensure extractor fan is running.")
            "Low" -> listOf("Low humidity can stress plants. Consider a brief misting or check for drafts.")
            else -> listOf("Humidity levels are good. Maintain gentle airflow.")
        }
        else -> listOf("Monitor readings regularly to spot trends.", "Keep a log of any changes you make to the system.")
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)

@Composable
private fun ValueHeader(
    @DrawableRes iconResId: Int,
    label: String,
    valueText: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    valueColor: Color = Color.White,
    statusChip: @Composable (() -> Unit)? = null,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = Color.White.copy(alpha = 0.05f),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .background(gradient)
                .padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(id = iconResId), null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, color = Color.LightGray)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            valueText,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = valueColor
                        )
                        if (unit.isNotBlank()) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                unit,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
                statusChip?.invoke()
            }
        }
    }
}

@Composable
private fun BigTile(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SRSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun TipsCard(title: String = "Tips", lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            lines.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray) }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        contentColor = color,
        shape = CircleShape
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(120.dp),
    stroke: Color = MaterialTheme.colorScheme.primary,
    grid: Color = Color.White.copy(alpha = 0.12f)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        for (i in 1..3) {
            val y = h * i / 4f
            drawLine(grid, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
        }

        if (values.size < 2) return@Canvas

        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 1f
        val stepX = w / (values.size - 1).toFloat()
        val path = Path()

        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = if (max == min) h / 2f else h - ((v - min) / (max - min) * h)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = stroke, style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun InfoStat(title: String, value: String) {
    Column(Modifier.padding(end = 16.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun PillSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 52.dp,
    height: Dp = 32.dp,
    thumbPadding: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val trackColor by animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else Color.Gray)
    val thumbColor by animateColorAsState(if (checked) Color.White else Color.LightGray)
    val thumbSize = height - thumbPadding * 2

    Box(
        modifier = modifier
            .size(width, height)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(interactionSource = interactionSource, indication = null) { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(thumbPadding)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun AlertDialogModel(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = null) },
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmation) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Dismiss") }
        }
    )
}