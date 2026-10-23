package ca.joelathiessen.kaly2.android

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import ca.joelathiessen.kaly2.android.repository.LocalRobotSessionApiService
import ca.joelathiessen.kaly2.android.repository.RobotSessionApiService
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
    fun provideAppConfig(application: Application): AppConfig {
        return AppConfig(application)
    }

    @Provides
    @Singleton
    fun provideMapImage(application: Application): AndroidJVMImage {
        val options = BitmapFactory.Options()
        options.inScaled = false
        return AndroidImage(BitmapFactory.decodeResource(
                application.resources, R.drawable.square_filled, options))
    }

    @Provides
    fun provideBluetoothSerialConnectionCreator(): BluetoothSerialConnectionCreator {
        return BluetoothSerialConnectionCreator()
    }

    @Provides
    @Singleton
    fun provideRobotSessionApiService(application: Application, mapImage: AndroidJVMImage,
                                      robotSerialConnectionCreator: BluetoothSerialConnectionCreator,
                                      sensorSerialConnectionCreator: BluetoothSerialConnectionCreator): RobotSessionApiService {
        robotSerialConnectionCreator.connectionName = application.getString(R.string.bluetooth_robot_name)
        sensorSerialConnectionCreator.connectionName = application.getString(R.string.bluetooth_sensor_name)

        val subscriberThreadExecutor: Executor = object : Executor {
            override fun execute(runnable: Runnable?) {
                Handler(application.mainLooper).post(runnable)
            }
        }

        return LocalRobotSessionApiService(KalyServer(mapImage, robotSerialConnectionCreator,
                sensorSerialConnectionCreator), subscriberThreadExecutor)
    }
}
