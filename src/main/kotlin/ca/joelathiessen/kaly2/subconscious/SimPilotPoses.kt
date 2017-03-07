package ca.joelathiessen.kaly2.subconscious

import ca.joelathiessen.kaly2.odometry.RobotPose

class SimPilotPoses(val realPose: RobotPose, odoPose: RobotPose): PilotPoses(odoPose)