package ca.joelathiessen.kaly2.core.planner

import ca.joelathiessen.kaly2.core.odometry.RobotPose

interface PathSegmentRootFactory {
    fun makePathSegmentRoot(startPose: RobotPose): PathSegment
}