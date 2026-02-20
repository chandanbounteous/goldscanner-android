package com.kanishk.goldscanner.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kanishk.goldscanner.presentation.ui.component.CompactGoldRateCard
import com.kanishk.goldscanner.presentation.component.CustomerListItem
import com.kanishk.goldscanner.presentation.component.AddCustomerModal
import com.kanishk.goldscanner.presentation.viewmodel.CustomerListViewModel
import com.kanishk.goldscanner.data.model.Customer
import org.koin.androidx.compose.koinViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    modifier: Modifier = Modifier,
    customerListViewModel: CustomerListViewModel = koinViewModel(),
    onNavigateToBasket: () -> Unit = {}
) {
    val customerUiState by customerListViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Show error toast
    customerUiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            customerListViewModel.clearError()
        }
    }
    
    // Show success toast
    customerUiState.showSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            customerListViewModel.clearSuccessMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Customers",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Gold Rate Card
        CompactGoldRateCard(
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Search Bar and Add Customer Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            OutlinedTextField(
                value = customerUiState.searchQuery,
                onValueChange = customerListViewModel::onSearchQueryChanged,
                placeholder = { 
                    Text(
                        "Search customers...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (customerUiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { customerListViewModel.onSearchQueryChanged("") }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            // Add Customer Button (Compact)
            IconButton(
                onClick = customerListViewModel::onAddNewCustomer,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add customer",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Customer List
        if (customerUiState.isLoading && customerUiState.customers.isEmpty()) {
            // Initial loading state
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else if (customerUiState.customers.isEmpty() && !customerUiState.isLoading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = if (customerUiState.searchQuery.isNotEmpty()) 
                            "No customers found" 
                        else 
                            "No customers yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customerUiState.searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your first customer to get started",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Customer list with items
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = customerUiState.customers,
                    key = { customer -> customer.id }
                ) { customer ->
                    var isLocked by remember { mutableStateOf(false) }
                    
                    CustomerListItem(
                        customer = customer,
                        isLockRateChecked = isLocked,
                        onLockRateChanged = { isLocked = it },
                        onSelectClicked = { 
                            customerListViewModel.onCustomerSelected(
                                customer = customer, 
                                isLockRateChecked = isLocked,
                                onNavigateToBasket = onNavigateToBasket
                            ) 
                        },
                        isCreatingBasket = customerUiState.creatingBasketForCustomerId == customer.id
                    )
                }
            }
        }
    }
    
    // Show success message
    customerUiState.showSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            // Show success toast or snackbar
            customerListViewModel.clearSuccessMessage()
        }
    }
    
    // Add Customer Modal
    if (customerUiState.showAddCustomerModal) {
        AddCustomerModal(
            firstName = customerUiState.customerForm.firstName,
            lastName = customerUiState.customerForm.lastName,
            phone = customerUiState.customerForm.phone,
            email = customerUiState.customerForm.email,
            firstNameError = customerUiState.customerForm.firstNameError,
            emailError = customerUiState.customerForm.emailError,
            isSubmitting = customerUiState.customerForm.isSubmitting,
            onFirstNameChanged = customerListViewModel::onFirstNameChanged,
            onLastNameChanged = customerListViewModel::onLastNameChanged,
            onPhoneChanged = customerListViewModel::onPhoneChanged,
            onEmailChanged = customerListViewModel::onEmailChanged,
            onSave = customerListViewModel::onCreateCustomer,
            onCancel = customerListViewModel::onCloseAddCustomerModal
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun CustomerScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = "Customers",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Search and Add Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Field
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        placeholder = { Text("Search customers...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { 
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Add Customer Button
                    FloatingActionButton(
                        onClick = { },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Add Customer",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Sample customer list with toggle state
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(3) { index ->
                        var isLocked by remember { mutableStateOf(index == 0) }
                        
                        CustomerListItem(
                            customer = Customer(
                                id = "${index + 1}",
                                firstName = when(index) {
                                    0 -> "John"
                                    1 -> "Sarah" 
                                    else -> "Michael"
                                },
                                lastName = when(index) {
                                    0 -> "Smith"
                                    1 -> "Johnson" 
                                    else -> "Brown"
                                },
                                phone = when(index) {
                                    0 -> "+1 234 567 8900"
                                    1 -> "+1 987 654 3210"
                                    else -> "+1 555 123 4567"
                                },
                                email = when(index) {
                                    0 -> "john@example.com"
                                    1 -> "sarah@example.com"
                                    else -> "michael@example.com"
                                },
                                createdAt = "2024-01-01T00:00:00Z",
                                updatedAt = "2024-01-01T00:00:00Z"
                            ),
                            isLockRateChecked = isLocked,
                            onLockRateChanged = { isLocked = it },
                            onSelectClicked = { }
                        )
                    }
                }
            }
        }
    }
}
