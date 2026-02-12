package com.rudra.isptechniciantool.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rudra.isptechniciantool.data.local.dao.CustomerDao
import com.rudra.isptechniciantool.data.local.dao.DeviceDao
import com.rudra.isptechniciantool.data.local.dao.LinkDao
import com.rudra.isptechniciantool.data.local.dao.RouterDao
import com.rudra.isptechniciantool.data.local.dao.SyncLogDao
import com.rudra.isptechniciantool.data.local.dao.TaskDao
import com.rudra.isptechniciantool.data.local.dao.WorkLogDao
import com.rudra.isptechniciantool.data.local.entity.CustomerEntity
import com.rudra.isptechniciantool.data.local.entity.DeviceEntity
import com.rudra.isptechniciantool.data.local.entity.LinkEntity
import com.rudra.isptechniciantool.data.local.entity.RouterEntity
import com.rudra.isptechniciantool.data.local.entity.SyncLogEntity
import com.rudra.isptechniciantool.data.local.entity.TaskEntity
import com.rudra.isptechniciantool.data.local.entity.WorkLogEntity

/**
 * Room database for the ISP Technician Tool application.
 * Stores all local data including customers, routers, sync logs, tasks, work logs,
 * network devices, and links for topology visualization.
 */
@Database(
    entities = [
        CustomerEntity::class,
        RouterEntity::class,
        SyncLogEntity::class,
        TaskEntity::class,
        WorkLogEntity::class,
        DeviceEntity::class,
        LinkEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class ISPDatabase : RoomDatabase() {
    
    abstract fun customerDao(): CustomerDao
    abstract fun routerDao(): RouterDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun taskDao(): TaskDao
    abstract fun workLogDao(): WorkLogDao
    abstract fun deviceDao(): DeviceDao
    abstract fun linkDao(): LinkDao
    
    companion object {
        const val DATABASE_NAME = "isp_technician_database"
    }
}
