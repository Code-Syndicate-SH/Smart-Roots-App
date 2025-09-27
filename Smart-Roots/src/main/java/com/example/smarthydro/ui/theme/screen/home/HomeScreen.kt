package com.example.smarthydro.ui.theme.screen.home

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.format.Formatter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.smarthydro.R
import com.example.smarthydro.models.SensorModel
import com.example.smarthydro.ui.theme.AutoBlue
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.ui.theme.GreenGood
import com.example.smarthydro.ui.theme.RedBad
import com.example.smarthydro.ui.theme.SO_OnSurf_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import com.example.smarthydro.ui.theme.SO_SurfVar_D
import com.example.smarthydro.ui.theme.screen.ReadingType
import com.example.smarthydro.viewmodels.ReadingViewModel
import com.example.smarthydro.viewmodels.SensorViewModel
import leagueSpartan

// ---------------------- Constants & Notifications ----------------------

const val GET_SENSOR_DATA_DELAY_MS: Long = 15 * 1000
private const val CHANNEL_ID = "sensor_alerts"
private const val PREFS_NAME = "com.example.smarthydro.PREFERENCE_FILE_KEY"
private const val LAST_NOTIFICATION_TIME_KEY = "last_notification_time"
private const val NOTIFICATION_INTERVAL_MS = 45 * 60 * 1000L // 45 minutes

fun pushNotification(context: Context, title: String, message: String) {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val lastNotificationTime = sharedPreferences.getLong(LAST_NOTIFICATION_TIME_KEY, 0L)
    val currentTime = System.currentTimeMillis()

    if (currentTime - lastNotificationTime < NOTIFICATION_INTERVAL_MS) return

    sharedPreferences.edit().putLong(LAST_NOTIFICATION_TIME_KEY, currentTime).apply()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val name = "Sensor Alerts"
        val descriptionText = "Notifications for sensor readings out of range"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    with(NotificationManagerCompat.from(context)) {
        notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

// ---------------------- Home Screen (Code 1 style + Code 2 data flow) ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SensorViewModel,
    navController: NavHostController,
    readingViewModel: ReadingViewModel
) {
    val sensorData by viewModel.sensorData.observeAsState(SensorModel())
    var language by remember { mutableStateOf("EN") }

    LaunchedEffect(Unit) {
        viewModel.fetchSensorPeriodically(GET_SENSOR_DATA_DELAY_MS)
    }

    val context = LocalContext.current
    LaunchedEffect(language) {
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            .edit()
            .putString("language_code", language)
            .apply()
    }

    val features = getFeatures(sensorData, language)

    Scaffold(
        containerColor = DeepBlue,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard", // avoid missing R.string.dashboard
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SO_Surf_D,
                    titleContentColor = SO_OnSurf_D,
                    navigationIconContentColor = SO_OnSurf_D
                )
            )
        }
    ) { pads ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBlue)
                .padding(pads)
        ) {
            WifiInfoSection(
                language = language,
                onToggleLanguage = { selected -> language = selected }
            )

            Image(
                painter = painterResource(id = R.drawable.greeting),
                contentDescription = "Greeting",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(features, key = { it.title }) { feature ->
                    MetricListTile(
                        iconRes = feature.iconId,
                        title = feature.title,
                        value = when {
                            feature.isCamera -> ""
                            feature.sensorReading.isEmpty() -> "––"
                            else -> feature.sensorReading
                        },
                        valueTint = readingTintFor(context, feature.title, feature.sensorReading),
                        onClick = {
                            when {
                                feature.isCamera -> {
                                    val cameraUrl = "http://192.168.235.20/viewer"
                                    val encodedUrl = Uri.encode(cameraUrl)
                                    navController.navigate("CameraStreamScreen/$encodedUrl")
                                }

                                feature.isNote -> {
                                    navController.navigate("notesScreen")
                                }

                                else -> {
                                    readingViewModel.setReadingType(
                                        ReadingType(feature.title, sensorData, "")
                                    )
                                    navController.navigate("viewData")
                                }
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

// ---------------------- Wifi & Language (from Code 2) ----------------------

@Composable
fun WifiInfoSection(language: String, onToggleLanguage: (String) -> Unit) {
    val context = LocalContext.current
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val locationPermissionGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    var isWifiEnabled by remember { mutableStateOf(wifiManager.isWifiEnabled) }

    DisposableEffect(Unit) {
        val wifiStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                    isWifiEnabled = wifiManager.isWifiEnabled
                }
            }
        }

        context.registerReceiver(
            wifiStatusReceiver,
            IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        )

        onDispose {
            context.unregisterReceiver(wifiStatusReceiver)
        }
    }

    if (!isWifiEnabled) {
        EnableWifiDialog(
            onEnableWifi = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) },
            onDismiss = {
                Toast.makeText(
                    context,
                    "WiFi is turned off. Please turn it on to use the application.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    } else {
        val message = if (locationPermissionGranted) {
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo
            val ssid = wifiInfo?.ssid
            val ipAddress = Formatter.formatIpAddress(wifiInfo?.ipAddress ?: 0)
            when {
                ssid != null && ssid != "<unknown ssid>" -> "🟢 Connected: $ssid"
                ssid == "<unknown ssid>" -> "🟠 $ssid, IP: $ipAddress"
                else -> "🔴 Not connected to WiFi"
            }
        } else {
            "🔴 Location permission is required to get WiFi details"
        }

        var expanded by remember { mutableStateOf(false) }
        val languages = listOf(
            "EN" to "English",
            "ZU" to "Zulu",
            "AF" to "Afrikaans",
            "XH" to "Xhosa",
            "ST" to "Sesotho",
            "TN" to "Setswana",
            "SS" to "siSwati",
            "VE" to "Tshivenda",
            "TS" to "Xitsonga",
            "NS" to "Northern Sotho",
            "ND" to "Ndebele"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier
                    .padding(1.dp)
                    .clickable { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
            )

            Box(
                modifier = Modifier
                    .border(BorderStroke(1.dp, AutoBlue), shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .padding(1.dp)
            ) {
                Text(
                    text = languages.find { it.first == language }?.second ?: "🇬🇧 English",
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(8.dp),
                    color = Color.White
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    languages.forEach { (code, label) ->
                        DropdownMenuItem(
                            onClick = {
                                onToggleLanguage(code)
                                expanded = false
                            },
                            text = { Text(text = label, color = Color.Black) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnableWifiDialog(onEnableWifi: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "WiFi is turned off") },
        text = { Text(text = "Would you like to turn on WiFi?") },
        confirmButton = {
            Button(
                onClick = { onEnableWifi() },
                colors = ButtonDefaults.buttonColors(AutoBlue, contentColor = Color.White),
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(Color(0xFF921E1E), contentColor = Color.White),
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Text(text = "No")
            }
        }
    )
}

// ---------------------- Features (from Code 2, unchanged data) ----------------------

@Composable
private fun getFeatures(sensorData: SensorModel, language: String): List<Feature> {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    val languageCode = sharedPreferences.getString("language_code", language)
    val features = when (languageCode) {
        "ZU" -> listOf(
            Feature(context.getString(R.string.Water_Flow_ZU), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_ZU), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_ZU), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_ZU), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_ZU), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_ZU), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "AF" -> listOf(
            Feature(context.getString(R.string.Water_Flow_AF), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_AF), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_AF), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_AF), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_AF), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_AF), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "XH" -> listOf(
            Feature(context.getString(R.string.Water_Flow_XH), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_XH), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_XH), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_XH), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_XH), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_XH), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "ST" -> listOf(
            Feature(context.getString(R.string.Water_Flow_ST), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_ST), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_ST), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_ST), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_ST), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_ST), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "TN" -> listOf(
            Feature(context.getString(R.string.Water_Flow_TN), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_TN), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_TN), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_TN), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_TN), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_TN), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "SS" -> listOf(
            Feature(context.getString(R.string.Water_Flow_SS), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_SS), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_SS), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_SS), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_SS), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_SS), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "VE" -> listOf(
            Feature(context.getString(R.string.Water_Flow_VE), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_VE), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_VE), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_VE), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_VE), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_VE), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "TS" -> listOf(
            Feature(context.getString(R.string.Water_Flow_TS), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_TS), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_TS), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_TS), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_TS), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_TS), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "NS" -> listOf(
            Feature(context.getString(R.string.Water_Flow_NS), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_NS), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_NS), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_NS), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_NS), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_NS), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        "ND" -> listOf(
            Feature(context.getString(R.string.Water_Flow_ND), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH_ND), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature_ND), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity_ND), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading_ND), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light_ND), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
        else -> listOf(
            Feature(context.getString(R.string.Water_Flow), R.drawable.ic_waterlv, com.example.smarthydro.ui.theme.BlueViolet1, com.example.smarthydro.ui.theme.BlueViolet2, com.example.smarthydro.ui.theme.BlueViolet3, sensorData.flowRate),
            Feature(context.getString(R.string.Water_pH), R.drawable.ic_cleanwater, com.example.smarthydro.ui.theme.Blue1, com.example.smarthydro.ui.theme.Blue2, com.example.smarthydro.ui.theme.Blue3, sensorData.pH),
            Feature(context.getString(R.string.Temperature), R.drawable.mode_fan_24px, com.example.smarthydro.ui.theme.Red1, com.example.smarthydro.ui.theme.Red2, com.example.smarthydro.ui.theme.Red3, sensorData.temperature),
            Feature(context.getString(R.string.Humidity), R.drawable.heat_pump_24px, com.example.smarthydro.ui.theme.Beige1, com.example.smarthydro.ui.theme.Beige2, com.example.smarthydro.ui.theme.Beige3, sensorData.humidity),
            Feature(context.getString(R.string.EC_Reading), R.drawable.ic_plant, com.example.smarthydro.ui.theme.LightGreen1, com.example.smarthydro.ui.theme.LightGreen2, com.example.smarthydro.ui.theme.LightGreen3, sensorData.eC),
            Feature(context.getString(R.string.Light), R.drawable.ic_light, com.example.smarthydro.ui.theme.OrangeYellow1, com.example.smarthydro.ui.theme.OrangeYellow2, com.example.smarthydro.ui.theme.OrangeYellow3, sensorData.light)
        )
    }
    return features + listOf(
        Feature(
            title = "Notes",
            iconId = R.drawable.menu_book_24px,
            mediumColor = Color.Transparent,
            lightColor = Color.Transparent,
            darkColor = Color.DarkGray,
            sensorReading = "",
            isNote = true
        ),
        Feature(
            title = "Camera",
            iconId = R.drawable.ic_camera,
            mediumColor = Color.Transparent,
            lightColor = Color.Transparent,
            darkColor = Color.DarkGray,
            sensorReading = "",
            isCamera = true
        )
    )
}

// ---------------------- Code-1-like tile ----------------------

@Composable
private fun MetricListTile(
    iconRes: Int,
    title: String,
    value: String,
    valueTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = SO_Surf_D,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SO_SurfVar_D,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = leagueSpartan,
                        color = SO_OnSurf_D
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (value.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = valueTint
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = SO_OnSurf_D.copy(alpha = 0.6f)
            )
        }
    }
}

// ---------------------- Reading tint (reuses your thresholds) ----------------------

private data class Range(val min: Float, val max: Float)

private fun readingTintFor(
    context: Context,
    title: String,
    readingValue: String
): Color {
    val reading = readingValue.toFloatOrNull() ?: return AutoBlue
    val range = acceptableRanges(context)[title]
    return if (range != null) {
        when {
            reading < range.min -> {
                pushNotification(context, title, "is very low ‼️")
                RedBad
            }
            reading > range.max -> {
                pushNotification(context, title, "is very high ‼️")
                RedBad
            }
            else -> GreenGood
        }
    } else {
        AutoBlue
    }
}

private fun acceptableRanges(context: Context): Map<String, Range> = mapOf(
    // Temperature
    context.getString(R.string.Temperature) to Range(18f, 25f),
    context.getString(R.string.Temperature_ZU) to Range(18f, 25f),
    context.getString(R.string.Temperature_AF) to Range(18f, 25f),
    context.getString(R.string.Temperature_XH) to Range(18f, 25f),
    context.getString(R.string.Temperature_ST) to Range(18f, 25f),
    context.getString(R.string.Temperature_TN) to Range(18f, 25f),
    context.getString(R.string.Temperature_SS) to Range(18f, 25f),
    context.getString(R.string.Temperature_VE) to Range(18f, 25f),
    context.getString(R.string.Temperature_TS) to Range(18f, 25f),
    context.getString(R.string.Temperature_NS) to Range(18f, 25f),
    context.getString(R.string.Temperature_ND) to Range(18f, 25f),

    // Water Flow
    context.getString(R.string.Water_Flow) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_ZU) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_AF) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_XH) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_ST) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_TN) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_SS) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_VE) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_TS) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_NS) to Range(10f, 100f),
    context.getString(R.string.Water_Flow_ND) to Range(10f, 100f),

    // Water pH
    context.getString(R.string.Water_pH) to Range(7f, 8f),
    context.getString(R.string.Water_pH_ZU) to Range(7f, 8f),
    context.getString(R.string.Water_pH_AF) to Range(7f, 8f),
    context.getString(R.string.Water_pH_XH) to Range(7f, 8f),
    context.getString(R.string.Water_pH_ST) to Range(7f, 8f),
    context.getString(R.string.Water_pH_TN) to Range(7f, 8f),
    context.getString(R.string.Water_pH_SS) to Range(7f, 8f),
    context.getString(R.string.Water_pH_VE) to Range(7f, 8f),
    context.getString(R.string.Water_pH_TS) to Range(7f, 8f),
    context.getString(R.string.Water_pH_NS) to Range(7f, 8f),
    context.getString(R.string.Water_pH_ND) to Range(7f, 8f),

    // Humidity
    context.getString(R.string.Humidity) to Range(65f, 75f),
    context.getString(R.string.Humidity_ZU) to Range(65f, 75f),
    context.getString(R.string.Humidity_AF) to Range(65f, 75f),
    context.getString(R.string.Humidity_XH) to Range(65f, 75f),
    context.getString(R.string.Humidity_ST) to Range(65f, 75f),
    context.getString(R.string.Humidity_TN) to Range(65f, 75f),
    context.getString(R.string.Humidity_SS) to Range(65f, 75f),
    context.getString(R.string.Humidity_VE) to Range(65f, 75f),
    context.getString(R.string.Humidity_TS) to Range(65f, 75f),
    context.getString(R.string.Humidity_NS) to Range(65f, 75f),
    context.getString(R.string.Humidity_ND) to Range(65f, 75f),

    // EC
    context.getString(R.string.EC_Reading) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_ZU) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_AF) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_XH) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_ST) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_TN) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_SS) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_VE) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_TS) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_NS) to Range(2f, 4f),
    context.getString(R.string.EC_Reading_ND) to Range(2f, 4f),

    // Light
    context.getString(R.string.Light) to Range(80f, 1000f),
    context.getString(R.string.Light_ZU) to Range(80f, 1000f),
    context.getString(R.string.Light_AF) to Range(80f, 1000f),
    context.getString(R.string.Light_XH) to Range(80f, 1000f),
    context.getString(R.string.Light_ST) to Range(80f, 1000f),
    context.getString(R.string.Light_TN) to Range(80f, 1000f),
    context.getString(R.string.Light_SS) to Range(80f, 1000f),
    context.getString(R.string.Light_VE) to Range(80f, 1000f),
    context.getString(R.string.Light_TS) to Range(80f, 1000f),
    context.getString(R.string.Light_NS) to Range(80f, 1000f),
    context.getString(R.string.Light_ND) to Range(80f, 1000f),
)

// ---------------------- (Optional) If you still need this id helper ----------------------

@StringRes
private fun getResourceIdForFeature(featureTitle: String): Int {
    return when (featureTitle) {
        "Water Flow" -> R.string.Water_Flow
        "Water pH" -> R.string.Water_pH
        "Temperature" -> R.string.Temperature
        "Humidity" -> R.string.Humidity
        "EC Reading" -> R.string.EC_Reading
        "Light" -> R.string.Light
        else -> R.string.Light
    }
}
