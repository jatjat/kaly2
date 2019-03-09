package ca.joelathiessen.android.kaly2android.ui.main

import ca.joelathiessen.android.kaly2android.repository.RobotSessionRepository
import ca.joelathiessen.kaly2.server.messages.RTRobotMsg
import ca.joelathiessen.kaly2.server.messages.RTSlamInfoMsg

class MainActivityPresenter(private val repository: RobotSessionRepository) {

    private var view: MainActivityView? = null
    private var iterations: MutableList<RTSlamInfoMsg> = ArrayList()

    init {
        repository.subscribeToRobotSession(null, { message ->
            onMessage(message)
        }, {
            repository.setRobotSessionSettings(true, {}, { failure ->
                print(failure)
            })
        }, { failure ->
            print(failure)
        })
    }

    private fun onMessage(message: RTRobotMsg) {
        when (message) {
            is RTSlamInfoMsg -> {
                iterations.add(message)
                view?.showIterations(iterations)
            }
        }
    }


    fun onViewAttached(view: MainActivityView) {
        this.view = view

        repository.getPastIterations({ pastIterations ->
            iterations = pastIterations?.toMutableList() ?: ArrayList()
            view.showIterations(iterations)
        }, { failure ->
            print(failure)
        })
    }

    fun onViewDetached() {
        view = null
    }
}