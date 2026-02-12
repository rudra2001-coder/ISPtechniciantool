package com.rudra.isptechniciantool

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Application class for ISP Technician Tool.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class ISPTechnicianApp : Application() {

    override fun onCreate() {
        super.onCreate()
        configureOSMDroid()
    }

    /**
     * Configure OSMDroid for map functionality.
     * Sets up tile cache location and size.
     */
    private fun configureOSMDroid() {
        val osmConfig = Configuration.getInstance()
        
        // Set user agent to identify the app
        osmConfig.userAgentValue = packageName
        
        // Set OSMDroid base path for tile cache
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        
        // Configure tile cache
        osmConfig.osmdroidTileCache = File(osmConfig.osmdroidBasePath, "tiles")
        
        // Set tile cache size (100MB)
        osmConfig.tileFileSystemCacheMaxBytes = 100L * 1024 * 1024
        osmConfig.tileFileSystemCacheTrimBytes = 80L * 1024 * 1024
        
        // Load configuration from shared preferences
        osmConfig.load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }
}
