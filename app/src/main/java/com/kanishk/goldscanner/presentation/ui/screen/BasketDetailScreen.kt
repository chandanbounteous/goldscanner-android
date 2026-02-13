package com.kanishk.goldscanner.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.kanishk.goldscanner.presentation.viewmodel.BasketDetailViewModel
import com.kanishk.goldscanner.data.model.response.BasketArticle
import com.kanishk.goldscanner.utils.Utils
import java.text.DecimalFormat

/**
 * Basket detail screen showing basket contents with reactive calculations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketDetailScreen(
    viewModel: BasketDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadBasketDetails()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basket Details") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            !uiState.errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadBasketDetails() }
                    ) {
                        Text("Retry")
                    }
                }
            }
            
            uiState.isDataLoaded -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Customer Information
                    uiState.customer?.let { customer ->
                        item {
                            CustomerInfoCard(
                                customerName = "${customer.firstName} ${customer.lastName ?: ""}".trim(),
                                customerPhone = customer.phone ?: "N/A"
                            )
                        }
                    }
                    
                    // Basket Articles
                    if (uiState.articles.isNotEmpty()) {
                        item {
                            Text(
                                text = "Articles in Basket",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(uiState.articles) { article ->
                            BasketArticleCard(
                                article = article,
                                onEditClick = { editArticle ->
                                    // TODO: Navigate to article edit screen
                                    // For now, we can log or show a toast
                                },
                                onDeleteClick = { deleteArticle ->
                                    // TODO: Implement article deletion from basket
                                    // For now, we can log or show a toast
                                }
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No articles in basket",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Calculation Inputs
                    item {
                        CalculationInputsCard(
                            oldGoldItemCost = uiState.oldGoldItemCostText,
                            extraDiscount = uiState.extraDiscountText,
                            isOldGoldItemCostValid = uiState.isOldGoldItemCostValid,
                            isExtraDiscountValid = uiState.isExtraDiscountValid,
                            onOldGoldItemCostChanged = viewModel::updateOldGoldItemCost,
                            onExtraDiscountChanged = viewModel::updateExtraDiscount
                        )
                    }
                    
                    // Basket Totals
                    item {
                        BasketTotalsCard(
                            preTaxAmount = uiState.preTaxBasketAmount,
                            luxuryTax = uiState.luxuryTax,
                            postTaxAmount = uiState.postTaxBasketAmount,
                            totalAddOnCost = uiState.totalAddOnCost,
                            totalBasketAmount = uiState.totalBasketAmount
                        )
                    }
                }
            }
        }
    }
    
    // Show success messages with snackbar
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Handle success message display if needed
            viewModel.clearMessages()
        }
    }
}

/**
 * Customer information card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerInfoCard(
    customerName: String,
    customerPhone: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Customer Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Name: $customerName",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Phone: $customerPhone",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Individual basket article card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketArticleCard(
    article: BasketArticle,
    onEditClick: (BasketArticle) -> Unit = {},
    onDeleteClick: (BasketArticle) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.articleCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Net Weight: ${Utils.formatDouble(article.netWeight)}g",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Karat: ${article.karat}K",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (article.addOnCost > 0) {
                        Text(
                            text = "Addon Cost: रु ${Utils.formatDouble(article.addOnCost)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onEditClick(article) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Article",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { onDeleteClick(article) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Article",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "रु ${Utils.formatDouble(article.postTaxArticleCost)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Calculation inputs card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationInputsCard(
    oldGoldItemCost: String,
    extraDiscount: String,
    isOldGoldItemCostValid: Boolean,
    isExtraDiscountValid: Boolean,
    onOldGoldItemCostChanged: (String) -> Unit,
    onExtraDiscountChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Basket Adjustments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = oldGoldItemCost,
                onValueChange = onOldGoldItemCostChanged,
                label = { Text("Old Gold Item Cost") },
                prefix = { Text("रु ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = !isOldGoldItemCostValid,
                supportingText = if (!isOldGoldItemCostValid) {
                    { Text("Please enter a valid positive number") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = extraDiscount,
                onValueChange = onExtraDiscountChanged,
                label = { Text("Extra Discount") },
                prefix = { Text("रु ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = !isExtraDiscountValid,
                supportingText = if (!isExtraDiscountValid) {
                    { Text("Please enter a valid positive number") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Basket totals card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketTotalsCard(
    preTaxAmount: Double,
    luxuryTax: Double,
    postTaxAmount: Double,
    totalAddOnCost: Double,
    totalBasketAmount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Basket Totals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pre-tax Amount:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "रु ${Utils.formatDouble(preTaxAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Luxury Tax:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "रु ${Utils.formatDouble(luxuryTax)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Post-tax Amount:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "रु ${Utils.formatDouble(postTaxAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Add On Cost:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "रु ${Utils.formatDouble(totalAddOnCost)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Final Amount:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "रु ${Utils.formatDouble(totalBasketAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}