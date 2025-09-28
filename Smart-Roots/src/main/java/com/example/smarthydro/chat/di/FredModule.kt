package com.example.smarthydro.chat.di

import com.example.smarthydro.BuildConfig
import com.example.smarthydro.chat.FredAgent
import com.example.smarthydro.chat.FredViewModel
import com.example.smarthydro.chat.GeminiClient
import com.example.smarthydro.chat.GeminiRestClient
import com.example.smarthydro.chat.tools.HarvestAdviceTool
import com.example.smarthydro.chat.tools.HarvestReminderTool
import com.example.smarthydro.chat.tools.Tool
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val fredModule = module {
    // Ktor client
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    // Gemini client (direct to Google with in-APK key)
    single<GeminiClient> {
        GeminiRestClient(
            http = get(),
            apiKeyProvider = { BuildConfig.GEMINI_API_KEY }
        )
    }

    // Tools
    factory<Tool> { HarvestAdviceTool() }
    factory<Tool> { HarvestReminderTool(androidContext()) }


    // Agent (collect all Tool bindings)
    single {
        val tools = getKoin().getAll<Tool>()
        FredAgent(get(), tools)
    }

    // ViewModel
    viewModel { FredViewModel(get()) }
}
