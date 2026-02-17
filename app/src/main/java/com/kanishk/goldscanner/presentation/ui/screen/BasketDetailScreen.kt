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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
//import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import org.koin.androidx.compose.koinViewModel
import com.kanishk.goldscanner.presentation.viewmodel.BasketDetailViewModel
import com.kanishk.goldscanner.data.model.response.BasketArticle
import com.kanishk.goldscanner.presentation.ui.component.CompactGoldRateCard
import com.kanishk.goldscanner.utils.Utils
import java.text.DecimalFormat

/**
 * Basket detail screen showing basket contents with reactive calculations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketDetailScreen(
    viewModel: BasketDetailViewModel = koinViewModel(),
    onNavigateToArticleListing: () -> Unit = {},
    onNavigateAway: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadBasketDetails()
    }
    
    // Clear active basket when navigating away from billed baskets
    DisposableEffect(Unit) {
        onDispose {
            if (uiState.basketDetail?.isBilled == true) {
                viewModel.clearActiveBasket()
                onNavigateAway()
            }
        }
    }
    
    var isBilled by remember { mutableStateOf(false) }
    
    // Show success messages with toast
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }
    
    // Show error messages with toast
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Basket Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    // Show Basket Listing Button - moved from FAB
                    IconButton(
                        onClick = { 
                            // TODO: Navigate to basket listing screen
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Show Basket List"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    contentPadding = PaddingValues(
                        start = 16.dp, 
                        end = 16.dp, 
                        top = 16.dp, 
                        bottom = 120.dp // Space for bottom buttons
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CompactGoldRateCard at the top
                    item {
                        CompactGoldRateCard(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Articles in Basket",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    // Add Article Button - Only shown for non-billed baskets
                                    if (uiState.basketDetail?.isBilled != true) {
                                        IconButton(
                                            onClick = { 
                                                onNavigateToArticleListing()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircle,
                                                contentDescription = "Add Article",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        items(uiState.articles) { article ->
                            BasketArticleCard(
                                article = article,
                                onEditClick = { editArticle ->
                                    // TODO: Navigate to article edit screen
                                },
                                onDeleteClick = { deleteArticle ->
                                    // Show confirmation dialog before deleting
                                    viewModel.showDeleteConfirmationDialog(deleteArticle)
                                }
                            )
                        }
                    } else {
                        item {
                            EmptyBasketCard(
                                onAddClick = {
                                    onNavigateToArticleListing()
                                }
                            )
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
                    

                    // Basket Status Card - moved below totals
                    item {
                        BasketStatusCard(
                            isBilled = isBilled,
                            onBilledChanged = { isBilled = it },
                            basketStatus = uiState.basketDetail?.isBilled?.let { if (it) "Billed" else "Not Billed" }
                        )
                    }
                    
                    // Bottom Action Buttons - Prominent buttons at the bottom
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Conditional button based on basket's billed status
                            if (uiState.basketDetail?.isBilled == true) {
                                // Print Bill Button - Only shown for billed baskets
                                Button(
                                    onClick = {
                                        // TODO: Implement print bill functionality
                                        Toast.makeText(context, "Print Bill functionality coming soon", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Print Bill",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            } else {
                                // Save/Bill Basket Button - Only shown for non-billed baskets
                                Button(
                                    onClick = {
                                        viewModel.saveBasket(isBilled)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isLoading && uiState.isFormValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uiState.isLoading) {
                                            if (isBilled) "Billing..." else "Saving..."
                                        } else {
                                            if (isBilled) "Bill Basket" else "Save Basket"
                                        },
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                            
                            // Discard Basket Button - Only shown for non-billed baskets
                            if (uiState.basketDetail?.isBilled != true) {
                                OutlinedButton(
                                    onClick = {
                                        // TODO: Implement discard basket functionality
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Discard Basket",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (uiState.showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmationDialog() },
            title = {
                Text(
                    text = "Delete Article",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove '${uiState.articleToDelete?.articleCode}' from this basket? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteArticle() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDeleteConfirmationDialog() }
                ) {
                    Text("Cancel")
                }
            }
        )
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit, // Using Edit as person icon not available
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Basket Adjustments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
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
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Basket Totals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
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

/**
 * Basket status card with billing option
 */
@Composable
fun BasketStatusCard(
    isBilled: Boolean,
    onBilledChanged: (Boolean) -> Unit,
    basketStatus: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBilled) MaterialTheme.colorScheme.primaryContainer 
                           else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isBilled) MaterialTheme.colorScheme.onPrimaryContainer 
                          else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Basket Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBilled) MaterialTheme.colorScheme.onPrimaryContainer 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isBilled,
                    onCheckedChange = onBilledChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mark as Billed",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isBilled) MaterialTheme.colorScheme.onPrimaryContainer 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
//            basketStatus?.let { status ->
//                Text(
//                    text = "Current Status: $status",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = if (isBilled) MaterialTheme.colorScheme.onPrimaryContainer
//                           else MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
        }
    }
}

/**
 * Empty basket card with helpful message
 */
@Composable
fun EmptyBasketCard(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Article",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "No articles in basket",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tap the + button to add articles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}