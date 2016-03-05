# kaly2
## A total refactor of the "kaly" FastSLAM &amp; Navigation project.

This project will enable simultaneous localization and mapping, and navigation, for a robot I have built.
leJOS is required to compile. The project can be opened in Eclipse.

Development is in-progress.

The code was architected to allow running it in a test environment on the PC, and on the EV3 programmable brick, while only changing wrapper code.
Different distance sensors are supported (IR, Ultrasound), and different feature gathering algorithms are allowed 
(currently implementing Split and Merge for feature detection, since it is very fast and accurate, especially compared to RANSAC).

Currently a "subconscious" thread gathers sensor information to be passed to the main thread, and the robot is given orders through a "commander" thread.
I've written some JUnit tests as well.


Todo:
  - write a super simple IC2 "driver" for a laser distance sensor
  - incorporate the FastSLAM implementation I previously wrote
  - implement a low-level object avoidance algorithm
  - implement a persistent obstacle map
  - incorporate a high-level obstacle avoidance algorithm such as the S-RRT* implementation I wrote.
  - implement a high-level "goal decider"
