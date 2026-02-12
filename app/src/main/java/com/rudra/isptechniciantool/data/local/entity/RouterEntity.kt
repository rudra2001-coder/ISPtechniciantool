package com.rudra.isptechniciantool.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.isptechniciantool.domain.model.Router
import java.time.LocalDateTime

/**
 * Room entity representing a MikroTik router configuration.
 */
@Entity(tableName = "routers")
data class RouterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "host")
    val host: String,
    
    @ColumnInfo(name = "port")
    val port: Int = 8728,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "password")
    val password: String,
    
    @ColumnInfo(name = "timeout")
    val timeout: Int = 3000,
    
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    
    @ColumnInfo(name = "last_successful_connect")
    val lastSuccessfulConnect: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Router {
        return Router(
            id = id,
            name = name,
            host = host,
            port = port,
            username = username,
            password = password,
            timeout = timeout,
            isEnabled = isEnabled,
            lastSuccessfulConnect = lastSuccessfulConnect?.let { 
                java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() 
            },
            createdAt = java.time.Instant.ofEpochMilli(createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(),
            modifiedAt = java.time.Instant.ofEpochMilli(modifiedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        )
    }
    
    companion object {
        fun fromDomainModel(router: Router): RouterEntity {
            return RouterEntity(
                id = router.id,
                name = router.name,
                host = router.host,
                port = router.port,
                username = router.username,
                password = router.password,
                timeout = router.timeout,
                isEnabled = router.isEnabled,
                lastSuccessfulConnect = router.lastSuccessfulConnect?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                createdAt = router.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                modifiedAt = router.modifiedAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
    }
}
