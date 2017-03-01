# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.
[![Build Status](https://travis-ci.org/jatjat/kaly2.svg?branch=master)](https://travis-ci.org/jatjat/kaly2)

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
To run as a webserver, invoke either:
```
./gradlew run
```
or:
```
./gradlew build && java -jar build/libs/kaly2-0.1.jar
```

To run the JUnit tests:
```
./gradlew test
```
