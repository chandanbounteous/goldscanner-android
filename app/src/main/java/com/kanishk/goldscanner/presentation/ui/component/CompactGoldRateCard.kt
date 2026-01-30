package com.kanishk.goldscanner.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kanishk.goldscanner.data.model.response.GoldRateResponse
import com.kanishk.goldscanner.data.model.response.NepaliDate
import com.kanishk.goldscanner.presentation.viewmodel.GoldRateViewModel
import com.kanishk.goldscanner.utils.NepaliDateFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun CompactGoldRateCard(
    modifier: Modifier = Modifier,
    viewModel: GoldRateViewModel = koinViewModel(),
    onError: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle authentication error
    if (uiState.isAuthenticationError) {
        onError()
        viewModel.clearAuthenticationError()
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when {
            uiState.isLoading -> {
                CompactGoldRateLoadingContent()
            }
            
            uiState.errorMessage != null -> {
                CompactGoldRateErrorContent(
                    onRetry = { viewModel.getCurrentGoldRate() }
                )
            }
            
            uiState.goldRateData != null -> {
                val goldRateData = uiState.goldRateData
                if (goldRateData != null) {
                    CompactGoldRateContent(goldRateData = goldRateData)
                }
            }
        }
    }
}

@Composable
private fun CompactGoldRateContent(
    goldRateData: GoldRateResponse
) {
    val gold24KRate = goldRateData.rates["24"]?.toInt() ?: 0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Gold rate information (single line)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Gold indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "24K Gold",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "रु ${String.format("%,d", gold24KRate)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        // Right side - Date information (single line)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Effective:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = NepaliDateFormatter.formatNepaliDate(goldRateData.date),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.width(2.dp))
            
            Text(
                text = "B.S.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CompactGoldRateLoadingContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Loading rates...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CompactGoldRateErrorContent(
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Failed to load rates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        
        TextButton(
            onClick = onRetry,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Retry",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactGoldRateCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview with data
            CompactGoldRateContent(
                goldRateData = GoldRateResponse(
                    rates = mapOf(
                        "24" to 150000.0,
                        "22" to 138000.0,
                        "18" to 112500.0,
                        "14" to 87450.0
                    ),
                    date = NepaliDate(
                        year = 2081,
                        month = 10,
                        dayOfMonth = 15
                    ),
                    lastUpdated = "2026-01-21T10:30:00.000Z"
                )
            )
            
            // Preview loading state
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                CompactGoldRateLoadingContent()
            }
            
            // Preview error state
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                CompactGoldRateErrorContent(onRetry = {})
            }
        }
    }
}