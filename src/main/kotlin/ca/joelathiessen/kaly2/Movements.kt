package ca.joelathiessen.kaly2

import lejos.robotics.navigation.Move
import lejos.robotics.navigation.MoveListener
import lejos.robotics.navigation.MoveProvider
import java.util.*


class Movements : MoveListener {
    private lateinit var moveInProg: Move
    private var moves: TreeMap<Long, Move> = TreeMap()//map of timestamps to their moves

    override fun moveStarted(move: Move, moveProvider: MoveProvider) {
        moveInProg = move
    }

    override fun moveStopped(move: Move, moveProvider: MoveProvider) {
        moves.put(move.timeStamp, move)
    }

    public fun getMovements(startTime: Long, endTime: Long = Long.MAX_VALUE): Collection<Move> {
        val wholeMovesMap = moves.subMap(startTime, true, endTime, true)
        val wholeMoves = wholeMovesMap.values
        val ans = ArrayList<Move>(wholeMoves.size)


        val itr = wholeMoves.iterator()
        val first = itr.next()
        val beforeFirstKV = moves.lowerEntry(wholeMovesMap.firstKey())
        val afterLastKV = moves.higherEntry(wholeMovesMap.lastKey())

        if (startTime < first.timeStamp && beforeFirstKV != null ){
            val beforeFirst = beforeFirstKV.value
            val duration = first.timeStamp - startTime
            val keepFraction = duration / (first.timeStamp - beforeFirst.timeStamp).toFloat()
            val generatedFirstMove = TimedMove(first.timeStamp, first.moveType, first.distanceTraveled * keepFraction,
                    first.angleTurned * keepFraction, first.travelSpeed, first.rotateSpeed, first.isMoving)
            ans.add(generatedFirstMove)
        }

        while ( itr.hasNext()) {
            ans.add(itr.next())
        }

        val last = wholeMoves.last()

        if(endTime > last.timeStamp && afterLastKV != null) {
            val afterLast = afterLastKV.value
            val duration = endTime - last.timeStamp
            val keepFraction = duration / (afterLast.timeStamp - last.timeStamp).toFloat()
            val generatedLastMove = TimedMove(last.timeStamp + duration, afterLast.moveType, afterLast.distanceTraveled * keepFraction,
                    afterLast.angleTurned * keepFraction, afterLast.travelSpeed, afterLast.rotateSpeed, afterLast.isMoving)
            ans.add(generatedLastMove)
        }

        return ans
    }
}