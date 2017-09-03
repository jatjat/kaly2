package ca.joelathiessen.kaly2.map

import lejos.robotics.geometry.Point

interface ReadableMap {
    fun getNearestObstacles(point: Point): Iterator<Point>
    fun getNearestObstacles(x: Float, y: Float): Iterator<Point>
}