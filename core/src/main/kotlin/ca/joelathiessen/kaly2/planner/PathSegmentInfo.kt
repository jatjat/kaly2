package ca.joelathiessen.kaly2.planner

import lejos.robotics.geometry.Line

interface PathSegmentInfo {
    fun getLines(): List<Line>
}