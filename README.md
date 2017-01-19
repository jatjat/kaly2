# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.
[![Build Status](https://travis-ci.org/jatjat/kaly2.svg?branch=master)](https://travis-ci.org/jatjat/kaly2)

### Introduction
This project will enable simultaneous localization and mapping, and navigation, for a robot I have built.
The project is written in Kotlin, and can be opened in Intellij IDEA.

Development is in-progress.

#### Completed:
- FastSLAM localization
- websocket intermediated control
- low-level object avoidance (algorithm & unit tests only)

#### Todo:
  - write a super simple IC2 "driver" for a laser distance sensor
  - implement a persistent obstacle map
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

### General Information
The code was architected to allow running it as a webserver on a PC, Android device, or on the EV3 programmable brick while only changing wrapper code.
Different distance sensors are allowed (IR, IR laser, Ultrasound), and different feature gathering algorithms are permitted.
(currently implementing Split and Merge for feature detection, since it is very fast and accurate, especially compared to RANSAC). I am indebted to SimpleSLAM for its matrix math and fast particle resampler.

Currently the codebase is split into two parts. In the first, a "subconscious" thread gathers sensor information to be passed to the main thread, and the robot is given orders through a "commander" thread. In the second, FastSLAM simulations are run on a webserver in response to websocket clients, on interacting threads.

#### Todo for version 0.2:
  - merge the first part of the codebase into the second
  - modernize the more old-school threading code
  - add tests for robot start/pause/unpause/reset/stop
  - add tests for setting FastSLAM settings
  - add tests for webserver and websocket communication
  - get rid of all warnings
  - simplify messaging code
