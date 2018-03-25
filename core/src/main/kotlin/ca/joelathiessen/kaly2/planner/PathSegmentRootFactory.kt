package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.kaly2.odometry.RobotPose

interface PathSegmentRootFactory {
    fun makePathSegmentRoot(startPose: RobotPose): PathSegment
}