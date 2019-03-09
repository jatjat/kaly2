package ca.joelathiessen.kaly2.core.planner.linear

import ca.joelathiessen.kaly2.core.odometry.RobotPose
import ca.joelathiessen.kaly2.core.planner.PathSegment
import ca.joelathiessen.kaly2.core.planner.PathSegmentRootFactory

class LinearPathSegmentRootFactory : PathSegmentRootFactory {
    override fun makePathSegmentRoot(startPose: RobotPose): PathSegment {
        return LinearPathSegment(startPose.x, startPose.y, null, 0.0f)
    }
}