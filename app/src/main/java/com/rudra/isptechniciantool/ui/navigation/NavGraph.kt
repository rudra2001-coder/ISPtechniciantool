package com.rudra.isptechniciantool.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rudra.isptechniciantool.ui.screens.backup.BackupScreen
import com.rudra.isptechniciantool.ui.screens.customer.AddCustomerScreen
import com.rudra.isptechniciantool.ui.screens.customer.CustomerDetailScreen
import com.rudra.isptechniciantool.ui.screens.customer.CustomerListScreen
import com.rudra.isptechniciantool.ui.screens.dashboard.DashboardScreen
import com.rudra.isptechniciantool.ui.screens.router.RouterSettingsScreen
import com.rudra.isptechniciantool.ui.screens.router.SecretsScreen
import com.rudra.isptechniciantool.ui.screens.tasks.AddTaskScreen
import com.rudra.isptechniciantool.ui.screens.tasks.TaskListScreen
import com.rudra.isptechniciantool.ui.screens.tools.IpCalculatorScreen
import com.rudra.isptechniciantool.ui.screens.tools.NetworkToolsScreen
import com.rudra.isptechniciantool.ui.screens.tools.PingToolScreen
import com.rudra.isptechniciantool.ui.screens.tools.PortScannerScreen

/**
 * Navigation graph for the application.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCustomerList = {
                    navController.navigate(Screen.CustomerList.route)
                },
                onNavigateToAddCustomer = {
                    navController.navigate(Screen.AddCustomer.route)
                },
                onNavigateToRouterSettings = {
                    navController.navigate(Screen.RouterSettings.route)
                },
                onNavigateToNetworkTools = {
                    navController.navigate(Screen.NetworkTools.route)
                },
                onNavigateToTaskList = {
                    navController.navigate(Screen.TaskList.route)
                },
                onNavigateToBackup = {
                    navController.navigate(Screen.Backup.route)
                },
                onNavigateToSecrets = {
                    navController.navigate(Screen.Secrets.route)
                }
            )
        }
        
        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onNavigateToAddCustomer = {
                    navController.navigate(Screen.AddCustomer.route)
                }
            )
        }
        
        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            CustomerDetailScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.EditCustomer.createRoute(id)) }
            )
        }
        
        composable(Screen.AddCustomer.route) {
            AddCustomerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EditCustomer.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
            // EditCustomerScreen would go here
            AddCustomerScreen(
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.RouterSettings.route) {
            RouterSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSyncClick = { navController.navigate(Screen.Secrets.route) }
            )
        }
        
        composable(Screen.Secrets.route) {
            SecretsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.NetworkTools.route) {
            NetworkToolsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPingTool = { navController.navigate(Screen.PingTool.route) },
                onNavigateToIpCalculator = { navController.navigate(Screen.IpCalculator.route) },
                onNavigateToPortScanner = { navController.navigate(Screen.PortScanner.route) }
            )
        }
        
        composable(Screen.PingTool.route) {
            PingToolScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.IpCalculator.route) {
            IpCalculatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PortScanner.route) {
            PortScannerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.TaskList.route) {
            TaskListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTask = { navController.navigate(Screen.AddTask.route) }
            )
        }
        
        composable(Screen.AddTask.route) {
            AddTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
