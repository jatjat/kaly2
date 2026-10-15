# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.

### Introduction
This project will enable simultaneous localization, mapping and navigation, for a robot I have built. It is intended to be run on the robot, an Android device, or a full-fleged server providing simulations to web-based clients. The code is mostly written in Kotlin.

Development is in-progress.

You can explore its currently exposed functionality [on my website](http://www.joelathiessen.ca)

#### Completed:
- FastSLAM localization
- websocket intermediated control using Jetty
- low-level object avoidance (algorithm & unit tests only)
- Split-and-Merge feature detection (algorithm & unit tests only)
- Linear RRT\* global planner (algorithm & some unit tests only)

#### Todo:
  - write a super simple IC2 "driver" for a laser distance sensor
  - implement a persistent obstacle map (perhaps using [requery](https://github.com/requery/requery)?)
  - incorporate a high-level obstacle avoidance algorithm such as the S-RRT\* implementation I wrote
  - do more sophisticated data association (e.g. MHT, JCBB, PDAF)
  - implement a high-level "goal decider"

### Running & Testing
Builds use [Bazel](https://bazel.build) through [Bazelisk](https://github.com/bazelbuild/bazelisk), which downloads the right Bazel version and JDKs.

Exposed 0.8.6 was only published on Bintray, which shut down in 2021, so first fetch it from the private Maven repo:
```
curl -fsS -H "Authorization: Bearer $(gcloud auth print-access-token)" -o lib/exposed-0.8.6.jar \
  https://us-central1-maven.pkg.dev/<project>/legacy-maven/org/jetbrains/exposed/exposed/0.8.6/exposed-0.8.6.jar
```

To run as a webserver:
```
bazel run //:server
```

To run the JUnit tests:
```
bazel test //...
```

To build the Docker image and load it into Docker as `kaly2:local`:
```
bazel run //:image_load
```
