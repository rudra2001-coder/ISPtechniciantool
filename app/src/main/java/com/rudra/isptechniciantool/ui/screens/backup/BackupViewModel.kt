package com.rudra.isptechniciantool.ui.screens.backup

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.Router
import com.rudra.isptechniciantool.domain.model.SyncStatus
import com.rudra.isptechniciantool.domain.model.Task
import com.rudra.isptechniciantool.domain.model.TaskPriority
import com.rudra.isptechniciantool.domain.model.TaskStatus
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import com.rudra.isptechniciantool.domain.repository.TaskRepository
import com.rudra.isptechniciantool.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * ViewModel for Backup and Export operations
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val routerRepository: RouterRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    /**
     * Export all data to JSON format
     */
    suspend fun exportToJson(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val backupData = JSONObject()
            
            // Export customers
            val customers = customerRepository.observeAllCustomers().first()
            val customersArray = JSONArray()
            
            for (customer in customers) {
                customersArray.put(customerToJson(customer))
            }
            backupData.put("customers", customersArray)
            
            // Export routers (without passwords)
            val routers = routerRepository.observeAllRouters().first()
            val routersArray = JSONArray()
            
            for (router in routers) {
                routersArray.put(routerToJson(router))
            }
            backupData.put("routers", routersArray)
            
            // Export tasks
            val tasks = taskRepository.observeAllTasks().first()
            val tasksArray = JSONArray()
            
            for (task in tasks) {
                tasksArray.put(taskToJson(task))
            }
            backupData.put("tasks", tasksArray)
            
            // Metadata
            val metadata = JSONObject()
            metadata.put("version", 1)
            metadata.put("createdAt", System.currentTimeMillis())
            metadata.put("appName", "ISP Technician Tool")
            backupData.put("metadata", metadata)
            
            // Write to file
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "isp_backup_$timestamp.json"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(backupData.toString(2).toByteArray())
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Export customers to CSV format
     */
    suspend fun exportCustomersToCsv(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val customers = customerRepository.observeAllCustomers().first()
            
            val csv = StringBuilder()
            
            // Header
            csv.append("ID,Name,Phone,Email,Username,IP Address,Package,Status,Sync Status,Created At\n")
            
            // Data rows
            for (customer in customers) {
                csv.append("${customer.id},")
                csv.append("\"${customer.name.escapeCsv()}\",")
                csv.append("\"${customer.phone.escapeCsv()}\",")
                csv.append("\"${customer.email ?: ""}\",")
                csv.append("\"${customer.username.escapeCsv()}\",")
                csv.append("\"${customer.ipAddress ?: ""}\",")
                csv.append("\"${customer.packageName ?: ""}\",")
                csv.append("${customer.customerStatus},")
                csv.append("${customer.syncStatus},")
                csv.append("\"${customer.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\"\n")
            }
            
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val fileName = "customers_$timestamp.csv"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { fos ->
                fos.write(csv.toString().toByteArray())
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restore data from a JSON file URI
     */
    suspend fun restoreFromUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !PermissionUtils.hasStoragePermission(context)) {
            return@withContext false
        }

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: return@withContext false
            
            val backupData = JSONObject(jsonString)
            
            // Restore customers
            val customersArray = backupData.optJSONArray("customers") ?: JSONArray()
            for (i in 0 until customersArray.length()) {
                val customerJson = customersArray.getJSONObject(i)
                val customer = jsonToCustomer(customerJson)
                
                // Check if customer exists
                val existing = customerRepository.getCustomerByUsername(customer.username)
                if (existing != null) {
                    // Update existing customer
                    customerRepository.updateCustomer(customer.copy(id = existing.id))
                } else {
                    // Create new customer
                    customerRepository.createCustomer(customer)
                }
            }
            
            // Restore routers (without sensitive data)
            val routersArray = backupData.optJSONArray("routers") ?: JSONArray()
            for (i in 0 until routersArray.length()) {
                val routerJson = routersArray.getJSONObject(i)
                val router = jsonToRouter(routerJson)
                
                // Check if router exists
                val existing = routerRepository.getRouterById(router.id)
                if (existing != null) {
                    // Update existing router (keep original password)
                    routerRepository.updateRouter(router.copy(id = existing.id, password = existing.password))
                } else {
                    // Create new router (with placeholder password)
                    if (router.password.isNotBlank()) {
                        routerRepository.createRouter(router)
                    }
                }
            }
            
            // Restore tasks
            val tasksArray = backupData.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArray.length()) {
                val taskJson = tasksArray.getJSONObject(i)
                val task = jsonToTask(taskJson)
                taskRepository.createTask(task)
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get a content URI for sharing a file
     */
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // Helper functions
    
    private fun customerToJson(customer: Customer): JSONObject {
        return JSONObject().apply {
            put("id", customer.id)
            put("name", customer.name)
            put("phone", customer.phone)
            put("phoneSecondary", customer.phoneSecondary ?: "")
            put("email", customer.email ?: "")
            put("address", customer.address)
            put("addressNotes", customer.addressNotes ?: "")
            put("username", customer.username)
            put("password", customer.password)
            put("ipAddress", customer.ipAddress ?: "")
            put("packageId", customer.packageId ?: 0)
            put("packageName", customer.packageName ?: "")
            put("status", customer.customerStatus.name)
            put("syncStatus", customer.syncStatus.name)
            put("notes", customer.notes ?: "")
            put("routerId", customer.routerId ?: 0)
        }
    }

    private fun routerToJson(router: Router): JSONObject {
        return JSONObject().apply {
            put("id", router.id)
            put("name", router.name)
            put("host", router.host)
            put("port", router.port)
            put("username", router.username)
            put("password", "") // Don't export password
            put("isEnabled", router.isEnabled)
        }
    }

    private fun taskToJson(task: Task): JSONObject {
        return JSONObject().apply {
            put("id", task.id)
            put("title", task.title)
            put("description", task.description ?: "")
            put("priority", task.priority.name)
            put("status", task.status.name)
            put("dueDate", task.dueDate?.let {
                it.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            } ?: 0)
            put("customerId", task.customerId ?: 0)
        }
    }

    private fun jsonToCustomer(json: JSONObject): Customer {
        return Customer(
            id = json.optLong("id", 0),
            name = json.getString("name"),
            phone = json.getString("phone"),
            phoneSecondary = json.optString("phoneSecondary").takeIf { it.isNotEmpty() },
            email = json.optString("email").takeIf { it.isNotEmpty() },
            address = json.getString("address"),
            addressNotes = json.optString("addressNotes").takeIf { it.isNotEmpty() },
            username = json.getString("username"),
            password = json.getString("password"),
            ipAddress = json.optString("ipAddress").takeIf { it.isNotEmpty() },
            packageId = json.optLong("packageId").takeIf { it > 0 },
            packageName = json.optString("packageName").takeIf { it.isNotEmpty() },
            customerStatus = try {
                CustomerStatus.valueOf(json.getString("status"))
            } catch (e: Exception) {
                CustomerStatus.ACTIVE
            },
            syncStatus = try {
                SyncStatus.valueOf(json.getString("syncStatus"))
            } catch (e: Exception) {
                SyncStatus.PENDING
            },
            notes = json.optString("notes").takeIf { it.isNotEmpty() },
            routerId = json.optLong("routerId").takeIf { it > 0 }
        )
    }

    private fun jsonToRouter(json: JSONObject): Router {
        return Router(
            id = json.optLong("id", 0),
            name = json.getString("name"),
            host = json.getString("host"),
            port = json.optInt("port", 8728),
            username = json.getString("username"),
            password = "", // Placeholder
            isEnabled = json.optBoolean("isEnabled", true)
        )
    }

    private fun jsonToTask(json: JSONObject): Task {
        return Task(
            id = json.optLong("id", 0),
            title = json.getString("title"),
            description = json.optString("description").takeIf { it.isNotEmpty() },
            priority = try {
                TaskPriority.valueOf(json.getString("priority"))
            } catch (e: Exception) {
                TaskPriority.MEDIUM
            },
            status = try {
                TaskStatus.valueOf(json.getString("status"))
            } catch (e: Exception) {
                TaskStatus.PENDING
            },
            dueDate = json.optLong("dueDate").takeIf { it > 0 }?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            },
            customerId = json.optLong("customerId").takeIf { it > 0 }
        )
    }

    private fun String.escapeCsv(): String {
        return this.replace("\"", "\"\"")
    }
}
