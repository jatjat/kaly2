package ca.joelathiessen.android.kaly2android.ui.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import ca.joelathiessen.android.kaly2android.R
import ca.joelathiessen.kaly2.server.messages.RTSlamInfoMsg
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

interface MainActivityView {
    fun showIterations(itrs: List<RTSlamInfoMsg>?)
}

class MainActivity : AppCompatActivity(), MainActivityView {
    @Inject lateinit var presenter: MainActivityPresenter

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

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
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
}
