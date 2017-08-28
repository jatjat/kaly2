package ca.joelathiessen.kaly2.planner

import ca.joelathiessen.util.itractor.ItrActorMsg

class PlannerPathsMsg(val paths: List<PathSegmentInfo>): ItrActorMsg()