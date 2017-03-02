package ca.joelathiessen.kaly2.subconscious.sensor

class SimSpinner(private val startAng: Float, private val endAng: Float, private val angIncr: Float): Spinnable {
    var angle = startAng
        private set

    override var spinning = false
        private set

    override var turningClockwise = false
        private set

    override fun spin() {
        spinning = true
    }

    fun incr() {
        if(!turningClockwise) {
            angle += angIncr
            if(angle > endAng) {
                angle = endAng
                turningClockwise = true
                spinning = false
            }
        } else {
            angle -= angIncr
            if(angle < startAng) {
                angle = startAng
                turningClockwise = false
                spinning = false
            }
        }
    }
}