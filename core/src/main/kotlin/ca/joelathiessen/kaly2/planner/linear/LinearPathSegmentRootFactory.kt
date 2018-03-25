package ca.joelathiessen.kaly2.planner.linear

import ca.joelathiessen.kaly2.odometry.RobotPose
import ca.joelathiessen.kaly2.planner.PathSegment
import ca.joelathiessen.kaly2.planner.PathSegmentRootFactory

class LinearPathSegmentRootFactory : PathSegmentRootFactory {
    override fun makePathSegmentRoot(startPose: RobotPose): PathSegment {
        return LinearPathSegment(startPose.x, startPose.y, null, 0.0f)
    }
}