package com.rudra.isptechniciantool.di

import android.content.Context
import androidx.room.Room
import com.rudra.isptechniciantool.data.local.dao.CustomerDao
import com.rudra.isptechniciantool.data.local.dao.DeviceDao
import com.rudra.isptechniciantool.data.local.dao.LinkDao
import com.rudra.isptechniciantool.data.local.dao.RouterDao
import com.rudra.isptechniciantool.data.local.dao.SyncLogDao
import com.rudra.isptechniciantool.data.local.dao.TaskDao
import com.rudra.isptechniciantool.data.local.dao.WorkLogDao
import com.rudra.isptechniciantool.data.local.database.ISPDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ISPDatabase {
        return Room.databaseBuilder(
            context,
            ISPDatabase::class.java,
            ISPDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCustomerDao(database: ISPDatabase): CustomerDao {
        return database.customerDao()
    }
    
    @Provides
    @Singleton
    fun provideRouterDao(database: ISPDatabase): RouterDao {
        return database.routerDao()
    }
    
    @Provides
    @Singleton
    fun provideSyncLogDao(database: ISPDatabase): SyncLogDao {
        return database.syncLogDao()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: ISPDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    @Singleton
    fun provideWorkLogDao(database: ISPDatabase): WorkLogDao {
        return database.workLogDao()
    }

    @Provides
    @Singleton
    fun provideDeviceDao(database: ISPDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    @Singleton
    fun provideLinkDao(database: ISPDatabase): LinkDao {
        return database.linkDao()
    }
}
