RunMyWay
========

### Introduction
This is a student project done by a group of students from National University of Singapore (NUS) who study the 
course of CS4274 (Multimedia and Mobile Networking).

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
