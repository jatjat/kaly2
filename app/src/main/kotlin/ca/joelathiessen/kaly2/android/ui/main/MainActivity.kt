package ca.joelathiessen.kaly2.android.ui.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import ca.joelathiessen.kaly2.android.R
import ca.joelathiessen.kaly2.core.server.messages.RTSlamInfoMsg
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import android.support.v4.app.ActivityCompat

interface MainActivityView {
    fun showIterations(itrs: List<RTSlamInfoMsg>?)
    fun createBTBond(address: String, pin: String)
}

class MainActivity : AppCompatActivity(), MainActivityView {
    @Inject lateinit var presenter: MainActivityPresenter

    private val devices = HashMap<String, BluetoothDevice>()
    private val PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    devices[device.address] = device
                    presenter.onBluetoothDeviceFound(device.address, device.name)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)

        registerReceiver(receiver, filter)
        BluetoothAdapter.getDefaultAdapter().startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewAttached(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.onViewDetached()
    }

    override fun showIterations(itrs: List<RTSlamInfoMsg>?) {
        // TODO: for now...
        val pose = itrs?.lastOrNull()?.bestPose
        if (pose != null) {
            message.setText(resources.getString(R.string.title_real_position, pose.x, pose.y, pose.theta))
        }
    }

    override fun createBTBond(address: String, pin: String) {
        val device = devices.get(address)
        device?.setPin(pin.toByteArray(charset("UTF-8")))
        device?.createBond()
    }
}
