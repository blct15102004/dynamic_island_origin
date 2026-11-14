package com.tuan.islebridge

import android.app.Application
import com.tuan.islebridge.core.originisland.OriginIslandAdapter
import com.tuan.islebridge.core.shizuku.ShizukuManager

class IsleBridgeApp : Application() {

    lateinit var shizukuManager: ShizukuManager
        private set

    lateinit var islandAdapter: OriginIslandAdapter
        private set

    override fun onCreate() {
        super.onCreate()
        islandAdapter = OriginIslandAdapter(this)
        shizukuManager = ShizukuManager(this).apply {
            initialize()
        }
    }

    override fun onTerminate() {
        shizukuManager.cleanup()
        super.onTerminate()
    }
}

