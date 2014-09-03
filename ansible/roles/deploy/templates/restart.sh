#!/bin/bash

kill -9 $(ps aux | grep '[j]ava' | grep janitor | awk '{print $2}')
#nohup java -jar /opt/janitor/janitor.jar

echo "Shoutout Janitor Process Has Been Restarted"