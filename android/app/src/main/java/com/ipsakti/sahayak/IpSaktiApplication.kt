package com.ipsakti.sahayak

import android.app.Application
import com.ipsakti.sahayak.data.api.RetrofitClient
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.data.manager.PersonaManager

class IpSaktiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        LanguageManager.init(this)
        PersonaManager.init(this)
    }
}
