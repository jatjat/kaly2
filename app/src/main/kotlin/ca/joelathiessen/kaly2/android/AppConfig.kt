package ca.joelathiessen.kaly2.android

import android.app.Application

class AppConfig(application: Application) {
    val bluetoothRobotName = application.getString(R.string.bluetooth_robot_name)
    val bluetoothRobotPin = application.getString(R.string.bluetooth_robot_pin)
    val bluetoothSensorName = application.getString(R.string.bluetooth_sensor_name)
    val bluetoothSensorPin = application.getString(R.string.bluetooth_sensor_pin)
}