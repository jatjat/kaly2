package ca.joelathiessen.kaly2.tests.pc.unit

import ca.joelathiessen.kaly2.Movements
import ca.joelathiessen.kaly2.TimedMove
import com.nhaarman.mockito_kotlin.mock
import lejos.robotics.navigation.MoveProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.junit.Assert.*

@RunWith(MockitoJUnitRunner::class)
class MovementsTest {

    @Test
    fun partialThenFullThenPartialMovements() {
        val moveProvider = mock<MoveProvider>()
        val movements = Movements()
        for (index in 0..6 step 2) {
            val move = TimedMove(index.toLong(), 1f, 0.5f, true)
            movements.moveStopped(move, moveProvider)
        }

        var selectedMovements = movements.getMovements(1L, 5L)

        val itrTS = selectedMovements.iterator()
        assertEquals(itrTS.next().timeStamp,2)
        assertEquals(itrTS.next().timeStamp,4)
        assertEquals(itrTS.next().timeStamp,5)

        val itrDist = selectedMovements.iterator()
        assertEquals(itrDist.next().distanceTraveled,0.5f)
        assertEquals(itrDist.next().distanceTraveled,1f)
        assertEquals(itrDist.next().distanceTraveled,0.5f)

        val itrAngle = selectedMovements.iterator()
        assertEquals(itrAngle.next().angleTurned,0.25f)
        assertEquals(itrAngle.next().angleTurned,0.5f)
        assertEquals(itrAngle.next().angleTurned,0.25f)
    }

    @Test
    fun tooWideTimeframe() {
        val moveProvider = mock<MoveProvider>()
        val movements = Movements()
        for (index in 2..8) {
            val move = TimedMove(index.toLong(), 1f, 0.5f, true)
            movements.moveStopped(move, moveProvider)
        }

        var selectedMovements = movements.getMovements(1L, 9L)

        assertEquals(selectedMovements.size, 6)
    }
}