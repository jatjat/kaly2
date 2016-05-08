package ca.joelathiessen.kaly2

import lejos.robotics.navigation.Move

class TimedMove : Move {
    private var actualTimeStamp = 0L

    constructor(timeStamp: Long, distance: Float, angle: Float, isMoving: Boolean) : super(distance, angle, isMoving) {
        actualTimeStamp = timeStamp
    }

    constructor(timeStamp: Long, type: MoveType?, distance: Float, angle: Float, travelSpeed: Float,
                rotateSpeed: Float, isMoving: Boolean) : super(type, distance, angle, travelSpeed, rotateSpeed,
            isMoving) {
        actualTimeStamp = timeStamp
    }

    constructor(timeStamp: Long, type: MoveType?, distance: Float, angle: Float, isMoving: Boolean)
    : super(type, distance, angle, isMoving) {
        actualTimeStamp = timeStamp
    }

    constructor(timeStamp: Long, isMoving: Boolean, angle: Float, turnRadius: Float)
    : super(isMoving, angle, turnRadius) {
        actualTimeStamp = timeStamp
    }

    override fun getTimeStamp(): Long {
        return actualTimeStamp //Overriding seems to be the least hacky (!) way to add arbitrary timing information to Move
    }
}