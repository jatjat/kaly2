package ca.joelathiessen.kaly2.core.subconscious

import ca.joelathiessen.kaly2.core.odometry.RobotPose

class SimPilotPoses(val realPose: RobotPose, odoPose: RobotPose) : PilotPoses(odoPose)