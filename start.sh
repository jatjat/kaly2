#!/bin/bash
#./gradlew build && java -XX:NewRatio=1.5 -Xms512m -Xmx800m -XX:MaxGCPauseMillis=5 -jar /home/joel/kaly2/build/libs/kaly2-0.1.jar
#./gradlew build && java -Xms128m -Xmx400m -XX:MaxGCPauseMillis=5 -jar /home/joel/kaly2/build/libs/kaly2-0.1.jar
#./gradlew build && java -XX:+UseG1GC -Xms512m -Xmx1024m -XX:MaxGCPauseMillis=5 -jar /home/joel/kaly2/build/libs/kaly2-0.1.jar
#gradle build -x test && java -XX:MaxGCPauseMillis=5 -jar /home/joel/kaly2/build/libs/kaly2-0.1.jar
#gradle build -x test && java -Xms768m -Xmx1024m -XX:MaxGCPauseMillis=5 -jar /home/joel/kaly2/build/libs/kaly2-0.1.jar
#gradle build -x test && java -Xms64m -Xmx256m -jar /home/joel/kaly2/build/libs/kaly2-0.1.jar
bazel build //:server_deploy.jar && java -XX:+UseConcMarkSweepGC -XX:CMSIncrementalSafetyFactor=50 -XX:+UseParNewGC -XX:+CMSConcurrentMTEnabled -XX:ConcGCThreads=2 -XX:ParallelGCThreads=2 -XX:GCTimeRatio=90 -XX:MaxGCPauseMillis=1 -XX:GCPauseIntervalMillis=1000 -jar bazel-bin/server_deploy.jar
