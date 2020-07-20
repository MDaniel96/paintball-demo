package demo.app.paintball

import android.app.Application
import android.content.Context

class PaintballApplication : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}