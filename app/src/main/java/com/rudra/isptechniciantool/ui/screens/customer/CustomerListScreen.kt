package com.rudra.isptechniciantool.ui.screens.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.SyncStatus

/**
 * Customer list screen with search and filter functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    viewModel: CustomerListViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search customers...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter is CustomerFilter.All,
                        onClick = { viewModel.selectFilter(CustomerFilter.All) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter is CustomerFilter.BySyncStatus && 
                                   (selectedFilter as CustomerFilter.BySyncStatus).syncStatus == SyncStatus.PENDING,
                        onClick = { viewModel.selectFilter(CustomerFilter.BySyncStatus(SyncStatus.PENDING)) },
                        label = { Text("Pending") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter is CustomerFilter.BySyncStatus && 
                                   (selectedFilter as CustomerFilter.BySyncStatus).syncStatus == SyncStatus.SYNCED,
                        onClick = { viewModel.selectFilter(CustomerFilter.BySyncStatus(SyncStatus.SYNCED)) },
                        label = { Text("Synced") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter is CustomerFilter.BySyncStatus && 
                                   (selectedFilter as CustomerFilter.BySyncStatus).syncStatus == SyncStatus.FAILED,
                        onClick = { viewModel.selectFilter(CustomerFilter.BySyncStatus(SyncStatus.FAILED)) },
                        label = { Text("Failed") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter is CustomerFilter.ByCustomerStatus && 
                                   (selectedFilter as CustomerFilter.ByCustomerStatus).customerStatus == CustomerStatus.ACTIVE,
                        onClick = { viewModel.selectFilter(CustomerFilter.ByCustomerStatus(CustomerStatus.ACTIVE)) },
                        label = { Text("Active") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Customer List
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (customers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No customers found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add your first customer to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = customers,
                        key = { it.id }
                    ) { customer ->
                        CustomerListItem(
                            customer = customer,
                            onClick = { onNavigateToCustomerDetail(customer.id) },
                            onDelete = { viewModel.deleteCustomer(customer) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerListItem(
    customer: Customer,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customer.address.isNotEmpty()) {
                        Text(
                            text = customer.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(horizontalAlignment = Alignment.End) {
                    SyncStatusBadge(syncStatus = customer.syncStatus)
                    Spacer(modifier = Modifier.height(4.dp))
                    CustomerStatusBadge(status = customer.customerStatus)
                }
            }
        }
    }
}

@Composable
private fun SyncStatusBadge(syncStatus: SyncStatus) {
    val (text, color) = when (syncStatus) {
        SyncStatus.SYNCED -> "Synced" to MaterialTheme.colorScheme.primary
        SyncStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
        SyncStatus.FAILED -> "Failed" to MaterialTheme.colorScheme.error
        SyncStatus.SYNCING -> "Syncing..." to MaterialTheme.colorScheme.secondary
        SyncStatus.CONFLICT -> "Conflict" to MaterialTheme.colorScheme.error
        SyncStatus.DELETING -> "Deleting" to MaterialTheme.colorScheme.outline
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun CustomerStatusBadge(status: CustomerStatus) {
    val (text, color) = when (status) {
        CustomerStatus.ACTIVE -> "Active" to MaterialTheme.colorScheme.primary
        CustomerStatus.PENDING -> "Pending" to MaterialTheme.colorScheme.tertiary
        CustomerStatus.SUSPENDED -> "Suspended" to MaterialTheme.colorScheme.error
        CustomerStatus.INACTIVE -> "Inactive" to MaterialTheme.colorScheme.outline
    }
    
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}
