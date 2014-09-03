#!/bin/sh
sbt clean assembly;
cp target/scala-2.10/shoutout-janitor.jar ./ansible/roles/deploy/files/janitor.jar
