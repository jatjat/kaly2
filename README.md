# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.
[![Build Status](https://travis-ci.org/jatjat/kaly2.svg?branch=master)](https://travis-ci.org/jatjat/kaly2)

This project will enable simultaneous localization and mapping, and navigation, for a robot I have built.
The project is written in Kotlin, and can be opened in Intellij IDEA

Development is in-progress.

To run as a webserver, invoke either:
```
./gradlew run
```
or:
```
./gradlew build && java -jar build/libs/kaly2-0.1.jar
```

To run the tests:
```
./gradlew test
```


The code was architected to allow running it as a webserver on a PC, an Android device, or on the EV3 programmable brick, while only changing wrapper code.
Different distance sensors are allowed (IR, IR laser, Ultrasound), and different feature gathering algorithms are permitted.
(currently implementing Split and Merge for feature detection, since it is very fast and accurate, especially compared to RANSAC).

I am indebted to SimpleSLAM for the matrix math.

Currently the codebase is split into two parts. In the first, a "subconscious" thread gathers sensor information to be passed to the main thread, and the robot is given orders through a "commander" thread. In the second FastSLAM simulations are run inside robots running on a webserver in response to websocket clients.

I've written some JUnit tests as well

Future work will involve merging the first part of the codebase into the second.


Todo:
  - write a super simple IC2 "driver" for a laser distance sensor
  - implement a low-level object avoidance algorithm
  - implement a persistent obstacle map
  - incorporate a high-level obstacle avoidance algorithm such as the S-RRT* implementation I wrote.
  - implement a high-level "goal decider"
