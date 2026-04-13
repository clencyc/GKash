package com.example.g_kash.investment.data

import android.util.Log
import com.example.g_kash.data.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface InvestmentRepository {
    suspend fun initiateStkPush(amount: Double, investmentType: String, phone: String): Result<StkPushResponse>
    suspend fun pollInvestmentStatus(checkoutRequestId: String): Result<InvestmentStatusResponse>
}

@Serializable
data class StkPushRequest(
    val phone: String,
    val amount: Double,
    val reference: String,
    val description: String
)

@Serializable
data class StkPushResponse(
    @SerialName("checkoutRequestID") val checkoutRequestId: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    val checkoutRequestIdFallback: String? = null,
    val message: String? = null,
    val errorCode: String? = null,
    val responseCode: String? = null
)

@Serializable
data class InvestmentStatusResponse(
    val confirmed: Boolean = false,
    val status: String? = null,
    @SerialName("transactionReference") val transactionReference: String? = null,
    val amount: Double? = null,
    val timestamp: String? = null,
    val message: String? = null,
    val errorCode: String? = null
)

private const val BASE_URL = "https://gkash.onrender.com/api"

class KtorInvestmentRepository(
    private val client: HttpClient,
    private val sessionStorage: SessionStorage
) : InvestmentRepository {

    override suspend fun initiateStkPush(amount: Double, investmentType: String, phone: String): Result<StkPushResponse> {
        return runCatching {
            val reference = "GKASH-${System.currentTimeMillis()}"
            val token = sessionStorage.authTokenStream.first()
            val response = client.post("$BASE_URL/payments/deposit") {
                contentType(ContentType.Application.Json)
                token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                setBody(
                    StkPushRequest(
                        phone = phone,
                        amount = amount,
                        reference = reference,
                        description = "$investmentType investment"
                    )
                )
            }

            if (response.status.isSuccess()) {
                val body = response.body<StkPushResponse>()
                Log.d(
                    "InvestmentRepository",
                    "STK push initiated: ${body.checkoutRequestId ?: body.transactionId ?: body.checkoutRequestIdFallback}"
                )
                body
            } else {
                val body = runCatching { response.body<StkPushResponse>() }.getOrNull()
                throw IllegalStateException(body?.message ?: "Failed to start investment payment")
            }
        }
    }

    override suspend fun pollInvestmentStatus(checkoutRequestId: String): Result<InvestmentStatusResponse> {
        return runCatching {
            val token = sessionStorage.authTokenStream.first()
            val response = client.get("$BASE_URL/payments/status/$checkoutRequestId") {
                token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }
            if (response.status.isSuccess()) {
                response.body<InvestmentStatusResponse>()
            } else {
                val body = runCatching { response.body<InvestmentStatusResponse>() }.getOrNull()
                throw IllegalStateException(body?.message ?: "Payment is still pending")
            }
        }
    }
}
