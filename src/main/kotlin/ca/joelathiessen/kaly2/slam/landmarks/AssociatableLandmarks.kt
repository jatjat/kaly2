package ca.joelathiessen.kaly2.slam.landmarks

import lejos.robotics.geometry.Point

interface AssociatableLandmarks {

    fun addLandmark(landmark: Landmark)

    fun getNearestNeighbor(point: Point): Landmark?
}