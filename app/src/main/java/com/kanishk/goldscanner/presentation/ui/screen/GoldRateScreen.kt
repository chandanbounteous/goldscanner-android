package com.kanishk.goldscanner.presentation.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kanishk.goldscanner.presentation.viewmodel.GoldRateViewModel
import com.kanishk.goldscanner.utils.NepaliDateFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldRateScreen(
    modifier: Modifier = Modifier,
    viewModel: GoldRateViewModel = koinViewModel(),
    onAuthenticationError: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle authentication error
    LaunchedEffect(uiState.isAuthenticationError) {
        if (uiState.isAuthenticationError) {
            onAuthenticationError()
            viewModel.clearAuthenticationError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Header
        Text(
            text = "Gold Rates",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 8.dp)
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.errorMessage != null -> {
                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    ErrorContent(
                        errorMessage = errorMessage,
                        onRetry = { viewModel.getCurrentGoldRate() }
                    )
                }
            }
            
            uiState.goldRateData != null -> {
                val goldRateData = uiState.goldRateData
                if (goldRateData != null) {
                    GoldRateContent(
                        goldRateData = goldRateData
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldRateContent(
    goldRateData: com.kanishk.goldscanner.data.model.response.GoldRateResponse
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Date information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Header information
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "CURRENT RATES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        letterSpacing = 1.2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Real-time pricing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // Center divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                )
                
                // Right side - Date information
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "EFFECTIVE DATE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        letterSpacing = 1.0.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = NepaliDateFormatter.formatNepaliDate(goldRateData.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.End
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "B.S. Calendar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
        
        // Gold rates table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Table title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gold Rates per Tola",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Table header with background
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PURITY",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "PRICE (रु)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                // Gold rates in descending order (24, 22, 18, 14)
                val sortedRates = goldRateData.rates.toList()
                    .sortedByDescending { it.first.toIntOrNull() ?: 0 }
                
                sortedRates.forEachIndexed { index, (karat, rate) ->
                    GoldRateRow(
                        karat = karat,
                        rate = rate,
                        isHighestPurity = index == 0,
                        isLastItem = index == sortedRates.size - 1
                    )
                    if (index < sortedRates.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun GoldRateRow(
    karat: String,
    rate: Double,
    isHighestPurity: Boolean = false,
    isLastItem: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isHighestPurity) {
                    Modifier.border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighestPurity) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighestPurity) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Karat information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Gold purity indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when (karat) {
                                "24" -> MaterialTheme.colorScheme.primary
                                "22" -> MaterialTheme.colorScheme.secondary
                                "18" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = karat,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "${karat}K Gold",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isHighestPurity) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = "${getPurityPercentage(karat)}% Pure",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isHighestPurity) {
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
                
                if (isHighestPurity) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⭐",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Right side - Price information
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "रु ${String.format("%,d", rate.toInt())}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighestPurity) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "per tola",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHighestPurity) {
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

/**
 * Helper function to get purity percentage for different karat values
 */
private fun getPurityPercentage(karat: String): String {
    return when (karat) {
        "24" -> "99.9"
        "22" -> "91.6"
        "18" -> "75.0"
        "14" -> "58.3"
        else -> "N/A"
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unable to fetch current rates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Try Again",
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}