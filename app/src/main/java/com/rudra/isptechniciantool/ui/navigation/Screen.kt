package com.rudra.isptechniciantool.ui.navigation

/**
 * Navigation routes for the application.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CustomerList : Screen("customer_list")
    object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    object AddCustomer : Screen("add_customer")
    object EditCustomer : Screen("edit_customer/{customerId}") {
        fun createRoute(customerId: Long) = "edit_customer/$customerId"
    }
    object RouterSettings : Screen("router_settings")
    object Secrets : Screen("secrets")
    object NetworkTools : Screen("network_tools")
    object PingTool : Screen("ping_tool")
    object IpCalculator : Screen("ip_calculator")
    object PortScanner : Screen("port_scanner")
    object TaskList : Screen("task_list")
    object AddTask : Screen("add_task")
    object Backup : Screen("backup")
}
