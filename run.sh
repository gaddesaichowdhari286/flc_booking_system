#!/bin/bash
# Launcher for Mac / Linux
# Sets locale to UTF-8 so the £ symbol renders correctly.
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
java -Dfile.encoding=UTF-8 -jar flc-booking-system.jar
