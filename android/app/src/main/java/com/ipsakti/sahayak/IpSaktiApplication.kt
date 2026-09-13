package com.ipsakti.sahayak

import android.app.Application
import com.ipsakti.sahayak.data.api.RetrofitClient

class IpSaktiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
