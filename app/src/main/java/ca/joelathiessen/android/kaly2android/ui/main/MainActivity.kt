package ca.joelathiessen.android.kaly2android.ui.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import ca.joelathiessen.android.kaly2android.R
import ca.joelathiessen.kaly2.server.KalyServer
import ca.joelathiessen.util.image.android.AndroidImage
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var mainActivityViewModel: MainActivityViewModel

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

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapImage = AndroidImage(BitmapFactory.decodeResource(
            applicationContext.resources, R.drawable.square_filled))

        val server = KalyServer(mapImage)
        server.serve()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
