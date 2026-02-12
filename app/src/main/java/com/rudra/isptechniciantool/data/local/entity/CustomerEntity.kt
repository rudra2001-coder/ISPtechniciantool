package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.Customer
import com.rudra.isptechniciantool.domain.model.CustomerStatus
import com.rudra.isptechniciantool.domain.model.SyncStatus
import java.time.LocalDateTime

/**
 * Room entity representing a customer in the local database.
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"]),
        Index(value = ["sync_status"]),
        Index(value = ["username"], unique = true),
        Index(value = ["customer_status"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "phone")
    val phone: String,
    
    @ColumnInfo(name = "phone_secondary")
    val phoneSecondary: String? = null,
    
    @ColumnInfo(name = "email")
    val email: String? = null,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "address_notes")
    val addressNotes: String? = null,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "password")
    val password: String,
    
    @ColumnInfo(name = "ip_address")
    val ipAddress: String? = null,
    
    @ColumnInfo(name = "package_id")
    val packageId: Long? = null,
    
    @ColumnInfo(name = "package_name")
    val packageName: String? = null,
    
    @ColumnInfo(name = "customer_status")
    val customerStatus: String = CustomerStatus.PENDING.name,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,
    
    @ColumnInfo(name = "install_date")
    val installDate: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "router_id")
    val routerId: Long? = null,
    
    @ColumnInfo(name = "sync_error")
    val syncError: String? = null,
    
    @ColumnInfo(name = "sync_retry_count")
    val syncRetryCount: Int = 0
) {
    fun toDomainModel(): Customer {
        return Customer(
            id = id,
            name = name,
            phone = phone,
            phoneSecondary = phoneSecondary,
            email = email,
            address = address,
            addressNotes = addressNotes,
            username = username,
            password = password,
            ipAddress = ipAddress,
            packageId = packageId,
            packageName = packageName,
            customerStatus = CustomerStatus.valueOf(customerStatus),
            syncStatus = SyncStatus.valueOf(syncStatus),
            installDate = installDate?.let { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() },
            createdAt = java.time.Instant.ofEpochMilli(createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
            modifiedAt = java.time.Instant.ofEpochMilli(modifiedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
            syncedAt = syncedAt?.let { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() },
            notes = notes,
            routerId = routerId,
            syncError = syncError,
            syncRetryCount = syncRetryCount
        )
    }
    
    companion object {
        fun fromDomainModel(customer: Customer): CustomerEntity {
            return CustomerEntity(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                phoneSecondary = customer.phoneSecondary,
                email = customer.email,
                address = customer.address,
                addressNotes = customer.addressNotes,
                username = customer.username,
                password = customer.password,
                ipAddress = customer.ipAddress,
                packageId = customer.packageId,
                packageName = customer.packageName,
                customerStatus = customer.customerStatus.name,
                syncStatus = customer.syncStatus.name,
                installDate = customer.installDate?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                createdAt = customer.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                modifiedAt = customer.modifiedAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                syncedAt = customer.syncedAt?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                notes = customer.notes,
                routerId = customer.routerId,
                syncError = customer.syncError,
                syncRetryCount = customer.syncRetryCount
            )
        }
    }
}
