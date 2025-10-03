package com.example.g_kash.di

import android.util.Log
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.authentication.data.ApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val API_BASE_URL = "https://gkash.onrender.com/api"

val networkModule = module {
    // Single instance of HttpClient for the entire application
    single<HttpClient> {
        HttpClient(Android.create()) { // Use Android engine
            // HttpClient Configuration

            // Install Logging
            install(Logging) {
                level = LogLevel.INFO // Use INFO for production, ALL for debugging
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
            }

            // Install Content Negotiation
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }

            // Set default request configurations
            defaultRequest {
                url(API_BASE_URL) // Use the standardized base URL
                header("Accept", "application/json")
            }

            engine {

            }
        }
    }

    // Bind ApiService to ApiServiceImpl, injecting the HttpClient
    single<ApiService> { ApiServiceImpl(get()) }
}
