# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.
[![Build Status](https://travis-ci.org/jatjat/kaly2.svg?branch=master)](https://travis-ci.org/jatjat/kaly2)

### Introduction
This project will enable simultaneous localization, mapping and navigation, for a robot I have built. It is intended to be run on the robot, an Android device, or a full-fleged server providing simulations to web-based clients. The code is mostly written in Kotlin.

Development is in-progress.

You can explore its currently exposed functionality [on my website](http://www.joelathiessen.ca)

#### Completed:
- FastSLAM localization (algorithm & unit tests)
- websocket intermediated control using Jetty
- Persistent obstacle map using mostly the Exposed ORM, with H2/MySQL (impl. & unit tests)
- low-level object avoidance (algorithm & unit tests)
- Split-and-Merge feature detection (algorithm & unit tests)
- Linear RRT\* global planner (algorithm & some unit tests)

#### Todo:
  - write a super simple IC2 "driver" for a laser distance sensor
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

To preview functionality not yet available on the website, you can run an acceptance test such as `MainLoopDemo` in `MainLoopView.kt` from an IDE or by writing a custom Gradle configuration.