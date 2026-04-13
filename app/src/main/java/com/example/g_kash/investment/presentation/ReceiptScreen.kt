package com.example.g_kash.investment.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.g_kash.investment.data.InvestmentReceipt
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    receipt: InvestmentReceipt,
    onDone: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Investment successful",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reference: ${receipt.transactionReference}")
                    Text("Amount: ${formatMoney(receipt.amount)}")
                    Text("Type: ${receipt.investmentType}")
                    Text("Date/Time: ${receipt.timestamp}")
                    Text("Status: ${receipt.status}")
                }
            }

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Done")
            }

            OutlinedButton(
                onClick = {
                    val shareText = buildString {
                        appendLine("GKash Investment Receipt")
                        appendLine("Reference: ${receipt.transactionReference}")
                        appendLine("Amount: ${formatMoney(receipt.amount)}")
                        appendLine("Type: ${receipt.investmentType}")
                        appendLine("Date/Time: ${receipt.timestamp}")
                        appendLine("Status: ${receipt.status}")
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share receipt"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share Receipt")
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
    return formatter.format(amount)
}
