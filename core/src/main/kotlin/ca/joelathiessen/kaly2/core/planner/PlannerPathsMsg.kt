package ca.joelathiessen.kaly2.core.planner

import ca.joelathiessen.util.itractor.ItrActorMsg

class PlannerPathsMsg(val paths: List<PathSegmentInfo>) : ItrActorMsg()