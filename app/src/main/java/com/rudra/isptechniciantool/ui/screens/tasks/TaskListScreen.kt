package com.rudra.isptechniciantool.ui.screens.tasks

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Task List screen for job tracking with enhanced UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onTaskClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // Filter and sort states
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var selectedSort by remember { mutableStateOf(SortOption.DUE_DATE) }

    // Mock data - replace with actual ViewModel
    val tasks = remember { generateMockTasks() }
    val filteredTasks = remember(tasks, selectedTab, searchQuery, selectedFilter, selectedSort) {
        filterAndSortTasks(tasks, selectedTab, searchQuery, selectedFilter, selectedSort)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Task Manager",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { showFilterMenu = true }) {
                            BadgedBox(
                                badge = {
                                    if (selectedFilter != null || searchQuery.isNotBlank()) {
                                        Badge {
                                            Text("1")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Filter Dropdown Menu
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Tasks") },
                                onClick = {
                                    selectedFilter = null
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Assignment, contentDescription = null)
                                }
                            )
                            TaskStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.displayName) },
                                    onClick = {
                                        selectedFilter = status
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            status.icon,
                                            contentDescription = null,
                                            tint = status.color
                                        )
                                    }
                                )
                            }
                        }

                        // Sort Dropdown Menu
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { sortOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row {
                                            Text(sortOption.displayName)
                                            if (selectedSort == sortOption) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedSort = sortOption
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Search Bar
                if (showSearchBar) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            placeholder = { Text("Search tasks...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    delay(1500) // Simulate network call
                    isRefreshing = false
                }
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Statistics Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatCard(
                        title = "Pending",
                        count = tasks.count { it.status == TaskStatus.PENDING },
                        icon = Icons.Outlined.AssignmentLate,
                        color = TaskStatus.PENDING.color,
                        modifier = Modifier.weight(1f)
                    )
                    TaskStatCard(
                        title = "In Progress",
                        count = tasks.count { it.status == TaskStatus.IN_PROGRESS },
                        icon = Icons.Outlined.Assignment,
                        color = TaskStatus.IN_PROGRESS.color,
                        modifier = Modifier.weight(1f)
                    )
                    TaskStatCard(
                        title = "Completed",
                        count = tasks.count { it.status == TaskStatus.COMPLETED },
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        color = TaskStatus.COMPLETED.color,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Status Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    listOf("All", "Active", "Completed").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Results count and active filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredTasks.size} task${if (filteredTasks.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedFilter != null) {
                        FilterChip(
                            selected = true,
                            onClick = { selectedFilter = null },
                            label = { Text(selectedFilter!!.displayName) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Task List
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when {
                                    searchQuery.isNotBlank() -> "No tasks match your search"
                                    selectedFilter != null -> "No ${selectedFilter!!.displayName.lowercase()} tasks"
                                    else -> "No tasks yet"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    searchQuery.isNotBlank() -> "Try adjusting your search"
                                    selectedFilter != null -> "Try selecting a different filter"
                                    else -> "Tap the + button to create your first task"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
                            )
                            if (searchQuery.isNotBlank() || selectedFilter != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        selectedFilter = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text("Clear Filters")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = filteredTasks,
                            key = { it.id }
                        ) { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task.id) }
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
}

@Composable
fun TaskStatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with status and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(task.status.color)
                    )

                    Text(
                        text = task.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = task.status.color,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Priority badge
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = task.priority.color.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = task.priority.color
                        )
                        Text(
                            text = task.priority.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = task.priority.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Client name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.clientName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category and due date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = task.category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (task.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(task.dueDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (task.isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Footer with task ID and attachments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Task #${task.id.takeLast(6)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )

                if (task.attachments.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${task.attachments.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// State variables
private var showSearchBar by mutableStateOf(false)

// Data Models
enum class TaskCategory(val displayName: String, val icon: ImageVector) {
    INSTALLATION("Installation", Icons.Outlined.Assignment),
    REPAIR("Repair", Icons.Outlined.AssignmentLate),
    MAINTENANCE("Maintenance", Icons.Outlined.Assignment),
    UPGRADE("Upgrade", Icons.Outlined.AssignmentTurnedIn),
    SURVEY("Survey", Icons.Outlined.Info)
}

enum class TaskPriority(val displayName: String, val color: Color) {
    LOW("Low", Color(0xFF4CAF50)),
    MEDIUM("Medium", Color(0xFFFF9800)),
    HIGH("High", Color(0xFFF44336))
}

enum class TaskStatus(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    PENDING("Pending", Color(0xFFFFA000), Icons.Outlined.AssignmentLate),
    IN_PROGRESS("In Progress", Color(0xFF2196F3), Icons.Outlined.Assignment),
    COMPLETED("Completed", Color(0xFF4CAF50), Icons.Outlined.AssignmentTurnedIn),
    CANCELLED("Cancelled", Color(0xFF9E9E9E), Icons.Outlined.Info)
}

enum class SortOption(val displayName: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    CREATED_DATE("Created Date"),
    CLIENT_NAME("Client Name")
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val clientName: String,
    val clientPhone: String = "",
    val clientAddress: String = "",
    val category: TaskCategory = TaskCategory.INSTALLATION,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: Long = System.currentTimeMillis() + 86400000 * 3, // 3 days from now
    val createdAt: Long = System.currentTimeMillis(),
    val attachments: List<TaskAttachment> = emptyList()
) {
    val isOverdue: Boolean
        get() = dueDate < System.currentTimeMillis() && status != TaskStatus.COMPLETED
}

data class TaskAttachment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val size: String,
    val uri: String = ""
)

// Helper functions
private fun filterAndSortTasks(
    tasks: List<Task>,
    tabIndex: Int,
    searchQuery: String,
    statusFilter: TaskStatus?,
    sortOption: SortOption
): List<Task> {
    return tasks
        .filter { task ->
            // Filter by tab
            when (tabIndex) {
                1 -> task.status == TaskStatus.PENDING || task.status == TaskStatus.IN_PROGRESS
                2 -> task.status == TaskStatus.COMPLETED
                else -> true
            }
        }
        .filter { task ->
            // Filter by status
            statusFilter?.let { task.status == it } ?: true
        }
        .filter { task ->
            // Filter by search query
            searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.clientName.contains(searchQuery, ignoreCase = true) ||
                    task.id.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith { a, b ->
            when (sortOption) {
                SortOption.DUE_DATE -> a.dueDate.compareTo(b.dueDate)
                SortOption.PRIORITY -> comparePriority(a.priority, b.priority)
                SortOption.CREATED_DATE -> b.createdAt.compareTo(a.createdAt)
                SortOption.CLIENT_NAME -> a.clientName.compareTo(b.clientName)
            }
        }
}

private fun comparePriority(p1: TaskPriority, p2: TaskPriority): Int {
    val order = mapOf(
        TaskPriority.HIGH to 0,
        TaskPriority.MEDIUM to 1,
        TaskPriority.LOW to 2
    )
    return order[p1]!!.compareTo(order[p2]!!)
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd", Locale.getDefault())
    return format.format(date)
}

// Mock data generator
private fun generateMockTasks(): List<Task> {
    val now = System.currentTimeMillis()
    return listOf(
        Task(
            title = "ONT Installation - Smith Residence",
            description = "Install new ONT and configure WiFi. Customer prefers afternoon slot.",
            clientName = "John Smith",
            clientPhone = "+1 234 567 8901",
            clientAddress = "123 Main St, Anytown, ST 12345",
            category = TaskCategory.INSTALLATION,
            priority = TaskPriority.HIGH,
            status = TaskStatus.PENDING,
            dueDate = now + 86400000, // tomorrow
            attachments = listOf(TaskAttachment(
                "Router_Config.pdf", "2.4 MB",
                size = "2.4 MB",
                uri = "https://example.com/router_config.pdf",
            ))
        ),
        Task(
            title = "Fiber Repair - Downtown Office",
            description = "Fiber cut reported in commercial district. Emergency repair required.",
            clientName = "ABC Corporation",
            clientPhone = "+1 234 567 8902",
            clientAddress = "456 Business Ave, Anytown, ST 12345",
            category = TaskCategory.REPAIR,
            priority = TaskPriority.HIGH,
            status = TaskStatus.IN_PROGRESS,
            dueDate = now - 3600000, // 1 hour ago (overdue)
            attachments = emptyList()
        ),
        Task(
            title = "Network Maintenance - Node 7",
            description = "Scheduled firmware upgrade for distribution node.",
            clientName = "ISP Technical",
            clientPhone = "+1 234 567 8903",
            clientAddress = "789 Tech Park, Anytown, ST 12345",
            category = TaskCategory.MAINTENANCE,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.COMPLETED,
            dueDate = now - 86400000, // yesterday
            attachments = listOf(
                TaskAttachment(
                    "Maintenance_Log.pdf", "1.1 MB",
                    size = "1.1 MB",
                    uri = "https://example.com/router_config.pdf"
                ),
                TaskAttachment(
                    "Config_Backup.bin", "512 KB",
                    size = "512 KB",
                    uri = "https://example.com/router_config.pdf"
                )
            )
        )
    )
}