package com.rudra.isptechniciantool.di

import com.rudra.isptechniciantool.data.repository.CustomerRepositoryImpl
import com.rudra.isptechniciantool.data.repository.RouterRepositoryImpl
import com.rudra.isptechniciantool.data.repository.SyncLogRepositoryImpl
import com.rudra.isptechniciantool.data.repository.TaskRepositoryImpl
import com.rudra.isptechniciantool.data.repository.WorkLogRepositoryImpl
import com.rudra.isptechniciantool.domain.repository.CustomerRepository
import com.rudra.isptechniciantool.domain.repository.RouterRepository
import com.rudra.isptechniciantool.domain.repository.SyncLogRepository
import com.rudra.isptechniciantool.domain.repository.TaskRepository
import com.rudra.isptechniciantool.domain.repository.WorkLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository
    
    @Binds
    @Singleton
    abstract fun bindRouterRepository(
        routerRepositoryImpl: RouterRepositoryImpl
    ): RouterRepository
    
    @Binds
    @Singleton
    abstract fun bindSyncLogRepository(
        syncLogRepositoryImpl: SyncLogRepositoryImpl
    ): SyncLogRepository
    
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
    
    @Binds
    @Singleton
    abstract fun bindWorkLogRepository(
        workLogRepositoryImpl: WorkLogRepositoryImpl
    ): WorkLogRepository
}
