package ca.joelathiessen.kaly2.core.slam.landmarks

import lejos.robotics.geometry.Point

interface AssociatableLandmarks {

    fun getNearestNeighbor(point: Point): Landmark?
}