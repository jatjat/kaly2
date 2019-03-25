package ca.joelathiessen.kaly2.android

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import ca.joelathiessen.kaly2.android.repository.LocalRobotSessionApiService
import ca.joelathiessen.kaly2.android.repository.RobotSessionApiService
import ca.joelathiessen.kaly2.core.ev3.SerialConnectionCreator
import ca.joelathiessen.kaly2.core.server.KalyServer
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
    fun provideSerialConnectionCreator(): SerialConnectionCreator {

        return BluetoothSerialConnectionCreator()
    }

    @Provides
    @Singleton
    fun provideRobotSessionApiService(application: Application, mapImage: AndroidJVMImage,
                                      robotSerialConnectionCreator: SerialConnectionCreator,
                                      sensorSerialConnectionCreator: SerialConnectionCreator): RobotSessionApiService {
        val subscriberThreadExecutor = object : Executor {
            override fun execute(runnable: Runnable?) {
                Handler(application.mainLooper).post(runnable)
            }
        }
        return LocalRobotSessionApiService(KalyServer(mapImage, robotSerialConnectionCreator,
                sensorSerialConnectionCreator), subscriberThreadExecutor)
    }
}

