package com.example.smarthydro.chat.di

import com.example.smarthydro.BuildConfig
import com.example.smarthydro.chat.*
import com.example.smarthydro.chat.tools.Tool
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val fredModule = module {
    single {
        HttpClient(Android) {
            expectSuccess = false
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }
        }
    }
    single<GeminiClient> {
        GeminiService(
            http = get(),
            apiKeyProvider = { BuildConfig.GEMINI_API_KEY }
        )
    }

    // Register tools if you have them, e.g.: factory<Tool> { HarvestReminderTool(androidContext()) }

    single { FredAgent(get(), getKoin().getAll<Tool>()) }
    viewModel { FredViewModel(get()) }
}
