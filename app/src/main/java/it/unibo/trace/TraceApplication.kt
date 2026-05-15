package it.unibo.trace

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.osmdroid.config.Configuration

class TraceApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // OSMDroid configuration
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", 0))

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@TraceApplication)
            modules(networkModule, dataModule, viewModelModule)
        }
    }
}
