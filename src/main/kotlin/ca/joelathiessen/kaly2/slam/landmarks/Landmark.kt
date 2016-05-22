package ca.joelathiessen.kaly2.slam.landmarks

import Jama.Matrix

class Landmark(var x: Double, var y: Double, var covariance: Matrix) {
}