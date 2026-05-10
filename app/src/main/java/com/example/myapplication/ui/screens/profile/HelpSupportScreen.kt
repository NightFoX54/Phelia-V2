package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.app.Application
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.HelpSupportViewModel

private val faqEntries = listOf(
    "Order tracking" to "Open Profile → My Orders to see status and tracking. Tap an order for full details.",
    "Return & refund" to "Contact the store from your order page first. If unresolved, open a ticket here with your ORD- reference.",
    "Payment failed" to "Check your card limit and try again. For repeated failures, include your order reference in a support ticket.",
    "Account security" to "Use a strong password and never share your login. Report suspicious activity with a ticket.",
)

@Composable
fun HelpSupportScreen(
    modifier: Modifier = Modifier,
    viewModel: HelpSupportViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application,
        ),
    ),
) {
    val customerName by viewModel.customerName.collectAsState()
    val customerEmail by viewModel.customerEmail.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    var search by remember { mutableStateOf("") }
    var orderRef by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    LaunchedEffect(submitState) {
        if (submitState is HelpSupportViewModel.SubmitUi.Success) {
            orderRef = ""
            message = ""
        }
    }

    val filteredFaq = remember(search) {
        val q = search.trim()
        if (q.isEmpty()) faqEntries
        else faqEntries.filter { (title, body) ->
            title.contains(q, ignoreCase = true) || body.contains(q, ignoreCase = true)
        }
    }

    if (submitState is HelpSupportViewModel.SubmitUi.Success) {
        AlertDialog(
            onDismissRequest = { viewModel.consumeSubmitMessage() },
            title = { Text("Ticket sent") },
            text = { Text("Our team will review your message and order details. You'll hear back at ${customerEmail.ifBlank { "your email" }}.") },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeSubmitMessage() }) {
                    Text("OK")
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Help articles", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search topics…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                )
                filteredFaq.forEach { (title, body) ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (filteredFaq.isEmpty()) {
                    Text("No matching topics.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Contact admin", fontWeight = FontWeight.Bold)
                Text(
                    "Create a ticket so our team can see your order and message. Paste your order ID or ORD-… label from My Orders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Signed in as ${customerName.ifBlank { "—" }} (${customerEmail.ifBlank { "—" }})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                OutlinedTextField(
                    value = orderRef,
                    onValueChange = {
                        orderRef = it
                        if (submitState is HelpSupportViewModel.SubmitUi.Error) viewModel.consumeSubmitMessage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Order reference") },
                    placeholder = { Text("e.g. ORD-Q1DSW8WZ or full order id") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        if (submitState is HelpSupportViewModel.SubmitUi.Error) viewModel.consumeSubmitMessage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Message to admin") },
                    placeholder = { Text("Describe what you need help with…") },
                    minLines = 5,
                    maxLines = 10,
                )
                val err = (submitState as? HelpSupportViewModel.SubmitUi.Error)?.message
                if (err != null) {
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { viewModel.submitTicket(orderRef, message) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = submitState !is HelpSupportViewModel.SubmitUi.Sending,
                ) {
                    if (submitState is HelpSupportViewModel.SubmitUi.Sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Submit ticket")
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Other ways to reach us", fontWeight = FontWeight.SemiBold)
                Text("support@phelia.app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "We reply to tickets and email during business hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
