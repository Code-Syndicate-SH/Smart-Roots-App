package com.example.smarthydro


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smarthydro.chat.FredScreen
import com.example.smarthydro.chat.di.fredModule
import com.example.smarthydro.domain.HapticFeedback
import com.example.smarthydro.ui.theme.AutoBlue
import com.example.smarthydro.ui.theme.DeepBlue
import com.example.smarthydro.ui.theme.SO_OnSurf_D
import com.example.smarthydro.ui.theme.SO_Primary_D
import com.example.smarthydro.ui.theme.SO_Surf_D
import com.example.smarthydro.ui.theme.SmartHydroTheme
import com.example.smarthydro.ui.theme.screen.AppBottomBar
import com.example.smarthydro.ui.theme.screen.NetworkLoadingScreen
import com.example.smarthydro.ui.theme.screen.home.AgeCameraScreen
import com.example.smarthydro.ui.theme.screen.home.AppSplashScreen
import com.example.smarthydro.ui.theme.screen.home.Dashboard
import com.example.smarthydro.ui.theme.screen.home.ImageScreen
import com.example.smarthydro.ui.theme.screen.note.NoteScreen
import com.example.smarthydro.ui.theme.screen.note.ViewNotes
import com.example.smarthydro.ui.theme.screen.note.WriteToNote
import com.example.smarthydro.ui.theme.screen.tent.TentSelectionScreen
import com.example.smarthydro.ui.theme.screen.viewData.SensorDetailScreen
import com.example.smarthydro.viewmodels.ComponentViewModel
import com.example.smarthydro.viewmodels.ImageViewModel
import com.example.smarthydro.viewmodels.ReadingViewModel
import com.example.smarthydro.viewmodels.SensorViewModel
import com.example.smarthydro.viewmodels.TentViewModel
import com.keagan.smartroots.screens.HomeScreen
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin


sealed class Destination(val route: String) {
    object Home : Destination("home")
    object ViewData : Destination("viewData")
    object NoteScreen : Destination("NoteScreen")
    object AgeCamera : Destination("Age")
    object SplashScreen : Destination("Splash")
    object Fred : Destination("Fred")
    object Image : Destination("Image/{macAddress}") {
        fun createRoute(macAddress: String) = "Image/$macAddress"
    }

    object TentManagement : Destination("TentManagement")
    object Dashboard : Destination("Dashboard")
    object DashboardWithMac : Destination("Dashboard/{macAddress}") {
        fun createRoute(macAddress: String) = "Dashboard/$macAddress"
    }

    object TentSelection : Destination("TentSelection/{filterType}") {
        fun createRoute(filterType: String) = "TentSelection/$filterType"
    }

    object Loading : Destination("Loading")

}

class MainActivity : ComponentActivity() {
    private val sensorViewModel: SensorViewModel by viewModels()
    private val component: ComponentViewModel by viewModels()
    private val reading: ReadingViewModel by viewModels()
    private val imageViewModel: ImageViewModel by viewModels()
    private val tentViewModel: TentViewModel by viewModels()
    override fun getApplicationContext(): Context? {
        return super.getApplicationContext()
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted && !isPermissionNotificationShown()) {
                pushNotification(
                    this,
                    "Permission Granted",
                    "You can now receive notifications.",
                    isSilent = false
                )
                setPermissionNotificationShown(true)
            }

            // After finishing with notification, continue to camera
            requestCameraPermissionIfNeeded()
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted && !hasRequiredCameraPermission()) {
                setCameraPermission(true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⬇️ NEW: Start Koin here (only once per process)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(application)
                modules(
                    // add your other modules here if you have them
                    fredModule
                )
            }
        }

       requestAllPermissions()

        setContent {
            SmartHydroTheme {
                val navController = rememberNavController()
                NavAppHost(
                    navController = navController,
                    sensorViewModel,
                    component,
                    reading,
                    context = applicationContext!!,
                    imageViewModel = imageViewModel,
                    tentViewModel = tentViewModel
                )
            }
        }
    }
    private fun requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Start with notifications
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {

                requestCameraPermissionIfNeeded()
            }
        } else {

            requestCameraPermissionIfNeeded()
        }
    }

    private fun requestCameraPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun hasRequiredCameraPermission(): Boolean {
        return getSharedPreferences(
            "camera_prefs",
            Context.MODE_PRIVATE
        ).getBoolean("camera_prefs_shown", false)
    }

    private fun setCameraPermission(shown: Boolean) {
        getSharedPreferences("camera_prefs", Context.MODE_PRIVATE).edit().apply {
            putBoolean("camera_prefs_shown", shown)
            apply()
        }
    }

    private fun pushNotification(
        context: Context,
        title: String,
        message: String,
        isSilent: Boolean = false,
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val CHANNEL_ID = "sensor_alerts"
        val NOTIFICATION_ID = 1

        if (isNotificationShown(NOTIFICATION_ID)) return

        createNotificationChannel(context, CHANNEL_ID, isSilent)
        val builder = buildNotification(context, CHANNEL_ID, title, message, isSilent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
            setNotificationShown(NOTIFICATION_ID, true)
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String, isSilent: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sensor Alerts"
            val descriptionText = "Notifications for sensor readings out of range"
            val importance =
                if (isSilent) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_HIGH
            val soundUri =
                if (isSilent) Uri.EMPTY else Uri.parse("android.resource://${context.packageName}/${R.raw.water_flow}")

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                setSound(soundUri, null)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        isSilent: Boolean,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isSilent) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setSound(if (isSilent) Uri.EMPTY else Uri.parse("android.resource://${context.packageName}/${R.raw.water_flow}"))
    }

    private fun isPermissionNotificationShown(): Boolean {
        return getSharedPreferences(
            "notification_prefs",
            Context.MODE_PRIVATE
        ).getBoolean("permission_notification_shown", false)
    }

    private fun setPermissionNotificationShown(shown: Boolean) {
        getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).edit().apply {
            putBoolean("permission_notification_shown", shown)
            apply()
        }
    }

    private fun isNotificationShown(notificationId: Int): Boolean {
        return getSharedPreferences(
            "notification_prefs",
            Context.MODE_PRIVATE
        ).getBoolean("notification_shown_$notificationId", false)
    }

    private fun setNotificationShown(notificationId: Int, shown: Boolean) {
        getSharedPreferences("notification_prefs", Context.MODE_PRIVATE).edit().apply {
            putBoolean("notification_shown_$notificationId", shown)
            apply()
        }
    }


    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,

            )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

@Composable
fun NavAppHost(
    navController: NavHostController,
    sensorViewModel: SensorViewModel,
    componentViewModel: ComponentViewModel,
    readingViewModel: ReadingViewModel,
    imageViewModel: ImageViewModel,
    context: Context,
    tentViewModel: TentViewModel,
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    val showBottomBar = when (currentDestination?.route) {
        Destination.Home.route,
        Destination.ViewData.route,
            -> true

        else -> false
    }
    val showTopBar = when (currentDestination?.route) {
        Destination.Home.route,
        Destination.SplashScreen.route,
        Destination.AgeCamera.route,
        Destination.Loading.route,
            -> false

        else -> true
    }
    Scaffold(
        containerColor = DeepBlue,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = SO_Primary_D)) {
                                    append("S")
                                }
                                append("mart ")

                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = AutoBlue
                                    )
                                ) {
                                    append("R")
                                }
                                append("oots")
                            },
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(0.9f),
                            textAlign = TextAlign.Center
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
        },
        bottomBar = {

            if (showBottomBar) {
                AppBottomBar(navController = navController)
            }

        }
    ) { padding ->

        NavHost(navController = navController, startDestination = Destination.SplashScreen.route) {


            composable(Destination.Dashboard.route) {
                Dashboard(
                    viewModel = sensorViewModel,
                    navController,
                    readingViewModel = readingViewModel,
                    macAddress = null,
                    padding = padding
                )
            }
            composable(Destination.ViewData.route) {
                SensorDetailScreen(
                    navController,
                    componentViewModel,
                    readingViewModel = readingViewModel,
                    sensorViewModel = sensorViewModel,

                    )
            }
            composable(
                route = Destination.TentSelection.route,
                arguments = listOf(navArgument("filterType") { type = NavType.StringType })
            ) { backStackEntry ->
                // Extract the filter argument from the route
                val filter = backStackEntry.arguments?.getString("filterType") ?: ""
                TentSelectionScreen(
                    navController = navController,
                    filterType = filter,
                    tentViewModel = tentViewModel, // Pass the ViewModel instance,
                    paddingValues = padding
                )
            }
            composable(Destination.NoteScreen.route) {
                NoteScreen(
                    navController = navController,
                    context
                )
            }
            composable("WriteToNote") { WriteToNote() }
            composable("ViewNotes") { ViewNotes() }


            composable(Destination.Fred.route) {
                FredScreen()

            }
            composable(
                Destination.Image.route,
                arguments = listOf(navArgument("macAddress") { type = NavType.StringType })
            ) {navBackStackEntry->
                val macAddress = navBackStackEntry.arguments?.getString("macAddress") ?: ""
                ImageScreen(imageViewModel = imageViewModel, macAddress, padding, tentViewModel)
            }
            composable(Destination.SplashScreen.route) {
                AppSplashScreen(navController)
            }
            composable(Destination.Home.route)
            {
                HomeScreen(navController)
            }
            composable(
                Destination.TentManagement.route,
                arguments = listOf(navArgument("macAddress") {
                    type =
                        NavType.StringType
                })
            ) { backstackEntry ->
                val filter = backstackEntry.arguments?.getString("filterType") ?: ""
                TentSelectionScreen(
                    tentViewModel = tentViewModel,
                    navController = navController,
                    filterType = filter,
                    paddingValues = padding
                )
            }
            //for remote mode specifically.
            composable(
                route = Destination.DashboardWithMac.route,
                arguments = listOf(navArgument("macAddress") { type = NavType.StringType })
            ) { backStackEntry ->
                val macAddress = backStackEntry.arguments?.getString("macAddress")
                Dashboard(
                    viewModel = sensorViewModel,
                    navController = navController,
                    readingViewModel = readingViewModel,
                    macAddress = macAddress, // remote mode,
                    padding = padding
                )
            }
            composable(route = Destination.Loading.route) {
                NetworkLoadingScreen(
                    onOnlineDetected = {
                        navController.navigate(Destination.Home.route)
                    },
                    onOfflineDetected = {
                        navController.navigate(Destination.Dashboard.route)
                    })
            }
            composable(route = Destination.AgeCamera.route) {
                AgeCameraScreen(navigateToLoadingScreen = {
                    val hapticFeedback = HapticFeedback()
                    hapticFeedback(context)
                    navController.navigate(Destination.Loading.route) {
                        popUpTo(Destination.AgeCamera.route) { inclusive = true }
                    }
                })
            }
        }
    }

}


