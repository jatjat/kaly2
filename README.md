# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.
[![Build Status](https://travis-ci.org/jatjat/kaly2.svg?branch=master)](https://travis-ci.org/jatjat/kaly2)

### Introduction
This project will enable simultaneous localization, mapping and navigation, for a robot I have built. The project is intended to be run on the robot, an Android device, or a full-fleged server providing simulations to web-based clients. It is mostly written in Kotlin.

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

### General Information
The code was architected to allow running it as a webserver on a PC, Android device, or on the EV3 programmable brick while only changing wrapper code.
It obeys the SOLID principles, and care was taken to ensure that components are both cohesive and loosely coupled. Different distance sensors are allowed (IR, IR laser, Ultrasound), different SLAM and feature gathering algorithms are permitted, and datastructures (e.g. the modified k-d tree) are abstracted. I am indebted to SimpleSLAM for its matrix math and fast particle resampler.

Currently the codebase is split into two parts. In the first, a "subconscious" thread gathers sensor information to be passed to the main thread, and the robot is given orders through a "commander" thread. In the second, FastSLAM simulations are run on a webserver in response to websocket clients, on interacting threads.

#### Todo for version 0.2:
  - merge the first part of the codebase into the second
  - modernize the more old-school threading code
  - add tests for robot start/pause/unpause/reset/stop
  - add tests for setting FastSLAM settings
  - add tests for webserver and websocket communication
  - simplify messaging code
