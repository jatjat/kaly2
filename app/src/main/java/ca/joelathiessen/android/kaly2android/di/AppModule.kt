package ca.joelathiessen.android.kaly2android.di

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import ca.joelathiessen.android.kaly2android.R
import ca.joelathiessen.android.kaly2android.repository.LocalRobotSessionApiService
import ca.joelathiessen.android.kaly2android.repository.RobotSessionApiService
import ca.joelathiessen.kaly2.server.KalyServer
import ca.joelathiessen.util.image.AndroidJVMImage
import ca.joelathiessen.util.image.android.AndroidImage
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideMapImage(application: Application): AndroidJVMImage {
        val options = BitmapFactory.Options()
        options.inScaled = false
        return AndroidImage(BitmapFactory.decodeResource(
                application.resources, R.drawable.square_filled, options))
    }

    @Provides
    @Singleton
    fun provideRobotSessionApiService(application: Application, mapImage: AndroidJVMImage): RobotSessionApiService {
        val subscriberThreadExecutor = object : Executor {
            override fun execute(runnable: Runnable?) {
                Handler(application.mainLooper).post(runnable)
            }
        }
        return LocalRobotSessionApiService(KalyServer(mapImage), subscriberThreadExecutor)
    }
}
