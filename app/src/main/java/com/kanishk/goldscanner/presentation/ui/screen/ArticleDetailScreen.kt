package com.kanishk.goldscanner.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kanishk.goldscanner.R
import com.kanishk.goldscanner.presentation.ui.component.CompactGoldRateCard
import com.kanishk.goldscanner.presentation.viewmodel.ArticleDetailViewModel
import com.kanishk.goldscanner.presentation.viewmodel.SharedEditArticleViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onNavigateBack: () -> Unit,
    articleDetailViewModel: ArticleDetailViewModel = koinViewModel()
) {
    // Get shared instance - using static instance approach
    val sharedEditViewModel: SharedEditArticleViewModel = remember { SharedEditArticleViewModel.getInstance() }
    val uiState by articleDetailViewModel.uiState.collectAsStateWithLifecycle()
    val selectedArticle by sharedEditViewModel.selectedArticle.collectAsStateWithLifecycle()
    var showKaratDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Initialize mode and populate article if editing
    LaunchedEffect(selectedArticle) {
        if (selectedArticle != null) {
            // Edit mode
            articleDetailViewModel.populateArticleForEdit(selectedArticle!!.article)
        } else {
            // Create mode
            articleDetailViewModel.setMode(ArticleDetailMode.CREATE_NEW)
        }
    }
    
    // Handle success message with toast and navigation
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // Show success toast
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_SHORT).show()
            
            // Clear messages and navigate back immediately
            articleDetailViewModel.clearMessages()
            sharedEditViewModel.clearSelectedArticle()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.headerTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        sharedEditViewModel.clearSelectedArticle()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Gold Rate Card
            CompactGoldRateCard()
            
            // Article Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    Text(
                        text = "Article Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Karat Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showKaratDropdown && uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT,
                        onExpandedChange = { 
                            if (uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT) {
                                showKaratDropdown = !showKaratDropdown 
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = "${uiState.karat}K",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Karat") },
                            trailingIcon = { 
                                if (uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showKaratDropdown) 
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                disabledTextColor = if (uiState.mode == ArticleDetailMode.UPDATE_INDEPENDENT) 
                                    MaterialTheme.colorScheme.onSurface else 
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            enabled = uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT
                        )
                        
                        if (uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT) {
                            ExposedDropdownMenu(
                                expanded = showKaratDropdown,
                                onDismissRequest = { showKaratDropdown = false }
                            ) {
                                uiState.karatOptions.forEach { karat ->
                                    DropdownMenuItem(
                                        text = { Text("${karat}K") },
                                        onClick = {
                                            articleDetailViewModel.updateKarat(karat)
                                            showKaratDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Gold Rate per Tola (Read Only)
                    OutlinedTextField(
                        value = formatCurrency(uiState.goldRateAsPerKaratPerTola),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Gold Rate / Tola (${uiState.karat}K)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Article Code
                    OutlinedTextField(
                        value = uiState.articleCode,
                        onValueChange = { 
                            if (uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT) {
                                articleDetailViewModel.updateArticleCode(it)
                            }
                        },
                        readOnly = uiState.mode == ArticleDetailMode.UPDATE_INDEPENDENT,
                        label = { Text("Article Code") },
                        placeholder = { Text("e.g., RNC1234") },
                        isError = !uiState.isArticleCodeValid && uiState.articleCode.isNotEmpty() && uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT,
                        supportingText = if (!uiState.isArticleCodeValid && uiState.articleCode.isNotEmpty() && uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT) {
                            { Text("Format: ABC1234 (3 letters + 4 digits)") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (uiState.mode == ArticleDetailMode.UPDATE_INDEPENDENT) 
                                MaterialTheme.colorScheme.onSurface else 
                                MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = uiState.mode != ArticleDetailMode.UPDATE_INDEPENDENT
                    )
                    
                    // Net Weight
                    OutlinedTextField(
                        value = uiState.netWeightText,
                        onValueChange = { text ->
                            articleDetailViewModel.updateNetWeightText(text)
                        },
                        label = { Text("Net Weight (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !uiState.isNetWeightValid,
                        supportingText = if (!uiState.isNetWeightValid) {
                            { Text("Must be between 0.01 and 999.00") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Gross Weight
                    OutlinedTextField(
                        value = uiState.grossWeightText,
                        onValueChange = { text ->
                            articleDetailViewModel.updateGrossWeightText(text)
                        },
                        label = { Text("Gross Weight (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !uiState.isGrossWeightValid,
                        supportingText = if (!uiState.isGrossWeightValid) {
                            { Text("Must be equal to net weight if no add-on cost, otherwise >= net weight") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Add On Cost
                    OutlinedTextField(
                        value = uiState.addOnCostText,
                        onValueChange = { text ->
                            articleDetailViewModel.updateAddOnCostText(text)
                        },
                        label = { Text("Add On Cost (Rs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !uiState.isAddOnCostValid,
                        supportingText = if (!uiState.isAddOnCostValid) {
                            { Text("Must be between 0 and 500,000") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Calculations Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    
                    Text(
                        text = "Calculated Values",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Wastage (Editable)
                    OutlinedTextField(
                        value = uiState.wastageText,
                        onValueChange = { text ->
                            articleDetailViewModel.updateWastageText(text)
                        },
                        label = { Text("Wastage (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !uiState.isWastageValid,
                        supportingText = if (!uiState.isWastageValid) {
                            { Text("Must be between 0 and 999.00") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    CalculationRow("Total Weight (g)", "${String.format("%.2f", uiState.totalWeight)}")
                    CalculationRow("Article Cost", formatCurrency(uiState.articleCostAsPerWeightAndKarat))
                    
                    // Making Charge (Editable)
                    OutlinedTextField(
                        value = uiState.makingChargeText,
                        onValueChange = { text ->
                            articleDetailViewModel.updateMakingChargeText(text)
                        },
                        label = { Text("Making Charge (Rs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !uiState.isMakingChargeValid,
                        supportingText = if (!uiState.isMakingChargeValid) {
                            { Text("Must be between 0 and 100,000") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Discount (Editable)
                    OutlinedTextField(
                        value = uiState.discountText,
                        onValueChange = { text ->
                            articleDetailViewModel.updateDiscountText(text)
                        },
                        label = { Text("Discount (Rs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !uiState.isDiscountValid,
                        supportingText = if (!uiState.isDiscountValid) {
                            { Text("Must be between 0 and 5,000") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Divider()
                    
                    CalculationRow("Pre Tax Cost", formatCurrency(uiState.articleCostBeforeTax))
                    CalculationRow("Luxury Tax", formatCurrency(uiState.luxuryTax))
                    CalculationRow("Post Tax Cost", formatCurrency(uiState.articleCostAfterTax))
                    
                    Divider()
                    
                    CalculationRow(
                        label = "Final Estimated Cost",
                        value = formatCurrency(uiState.finalEstimatedCost),
                        isHighlighted = true
                    )
                }
            }
            
            // Action Buttons
            when (uiState.mode) {
                ArticleDetailMode.CREATE_NEW -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                sharedEditViewModel.clearSelectedArticle()
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = { articleDetailViewModel.saveArticle() },
                            enabled = uiState.isFormValid && !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save Article")
                            }
                        }
                    }
                }
                ArticleDetailMode.UPDATE_INDEPENDENT -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    sharedEditViewModel.clearSelectedArticle()
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = { articleDetailViewModel.saveArticle() },
                                enabled = uiState.isFormValid && !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Update Article")
                                }
                            }
                        }
                        
                        // Only show "Save to basket" button when there's an active basket
                        if (uiState.hasActiveBasket) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = { 
                                    // TODO: Implement save to basket functionality
                                },
                                enabled = uiState.isFormValid && !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.gold_basket),
                                            contentDescription = "Add to basket",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Save To Basket")
                                    }
                                }
                            }
                        }
                    }
                }
                ArticleDetailMode.UPDATE_BASKET_ITEM -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    sharedEditViewModel.clearSelectedArticle()
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { 
                                // TODO: Implement save to basket functionality
                            },
                            enabled = uiState.isFormValid && !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.gold_basket),
                                        contentDescription = "Add to basket",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
            
            // Show messages
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculationRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isHighlighted) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "NP"))
    return "Rs ${formatter.format(amount)}"
}