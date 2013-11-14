RunMyWay
========

### Introduction
This is a student project done by a group of students from National University of Singapore (NUS) who study the 
course of CS4274 (Multimedia and Mobile Networking).
Its product is an Android application for runners to better schedule and monitor their running exercises.

### Acknowledgement
Thanks to Assoc Prof [Pung Hung Keng](https://www.comp.nus.edu.sg/~punghk/)
and Cavatur Pranav Phalgun for their assistance and review on this project.

### Setup

Include the External JAR files into libraries of Build Path and export them in the order below.
The order is to ensure the dependencies among the libraries.
All the Google API files are inside **libs\_calendar** folder.

* google-api-client-1.17.0-rc.jar
* google-api-client-android-1.17.0-rc.jar
* google-oauth-client-1.17.0-rc.jar
* google-http-client-1.17.0-rc.jar
* google-http-client-android-1.17.0-rc.jar
* google-http-client-jackson2-1.17.0-rc.jar
* gson-2.1.jar
* jackson-core-asl-1.9.4.jar
* jackson-core-2.1.3.jar
* jsr305-1.3.9.jar
* protobuf-java-2.2.0.jar
* google-api-service-calendar.jar

Right click the following two folders in the Package Explorer, 
select "Properties" and check the "Is Library" option. 
Then select "Properties" of the project and
choose "Android".
Set the API version to 4.3 and add these two folders into the project as libraries:

* google-play-services\_lib
* GraphView-master

To use Google Map API

### Features

* Generate and record real-time statistics during users’ running exercises.
* Provide graphic display for users’ exercise histories.
* Determine and recommend optimal schedule based on analysis of the users’ exercise history,
weather forecast and upcoming events in the users’ calendar.
* Detect possible collisions of exercise schedule with changes in the users’ calendar or weather,
and re-schedule sessions where necessary.
* Push notifications to the users before the start of a scheduled exercise session
if the application believes that there is a high probability that the user will follow the schedule.
This decision is made based on user’s current context such as location and weather.
* Monitor the completeness of the schedule and notify the users of missed sessions.
The users will decide whether or not to re-schedule the missed sessions.

### Contact developers
To report any issue in the program,
click [here](https://github.com/YGLiu/RunMyWay/issues).

To discuss with developers,
email us via [cs4274group7@gmail.com](cs4274group7@gmail.com).
