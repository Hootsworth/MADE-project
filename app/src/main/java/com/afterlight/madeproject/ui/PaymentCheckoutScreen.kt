package com.afterlight.madeproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.ui.theme.Coal
import com.afterlight.madeproject.ui.theme.Copper
import com.afterlight.madeproject.ui.theme.GatherTypography
import com.afterlight.madeproject.ui.theme.LightTextMuted
import com.afterlight.madeproject.ui.theme.Moss
import com.afterlight.madeproject.ui.theme.Pearl
import com.afterlight.madeproject.ui.theme.Sand
import com.afterlight.madeproject.ui.theme.Snow

@Composable
fun PaymentCheckoutScreen(
    onBackClick: () -> Unit,
    viewModel: PaymentCheckoutViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsStateWithLifecycle()
    val processing by viewModel.processing.collectAsStateWithLifecycle()
    val completed by viewModel.completed.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    var cardholder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
    var expiry by remember { mutableStateOf("12/28") }
    var cvv by remember { mutableStateOf("123") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmoothButton(
                text = "Back",
                onClick = onBackClick,
                containerColor = Pearl.copy(alpha = 0.5f),
                contentColor = Coal,
                modifier = Modifier.fillMaxWidth(0.3f)
            )
            Text(text = "Dummy Checkout", style = GatherTypography.titleLarge, color = Coal)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "This is a test payment page", style = GatherTypography.titleLarge, color = Coal)
                Text(
                    text = "No real money is collected. Completing this screen just confirms the RSVP for a paid event.",
                    style = GatherTypography.bodyLarge,
                    color = LightTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Sand.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        event?.let {
                            Text(text = it.title.ifBlank { "Untitled Event" }, style = GatherTypography.titleLarge, color = Coal)
                            Text(text = "Amount due: ₹${it.price.ifBlank { "0" }}", style = GatherTypography.labelLarge, color = Copper)
                        } ?: Text(text = "Loading event details...", style = GatherTypography.bodyMedium, color = LightTextMuted)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Payment Details", style = GatherTypography.titleLarge, color = Coal)

                SmoothTextField(
                    value = cardholder,
                    onValueChange = { cardholder = it },
                    label = "Cardholder Name"
                )
                SmoothTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = "Card Number"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmoothTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = "Expiry",
                        modifier = Modifier.weight(1f)
                    )
                    SmoothTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        label = "CVV",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (status != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (completed) Moss.copy(alpha = 0.15f) else Sand.copy(alpha = 0.2f)
            ) {
                Text(
                    text = status.orEmpty(),
                    style = GatherTypography.bodyMedium,
                    color = if (completed) Moss else Coal,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        SmoothButton(
            text = when {
                completed -> "Payment Approved"
                processing -> "Processing..."
                else -> "Complete Payment"
            },
            onClick = { viewModel.completePayment() },
            enabled = !processing && !completed,
            containerColor = if (completed) Moss else Copper,
            contentColor = Snow,
            modifier = Modifier.fillMaxWidth()
        )

        if (completed) {
            SmoothButton(
                text = "Back to Event",
                onClick = onBackClick,
                containerColor = Pearl.copy(alpha = 0.5f),
                contentColor = Coal,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}