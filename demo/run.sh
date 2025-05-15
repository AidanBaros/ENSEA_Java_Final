#!/bin/bash

mvn clean compile

java -XstartOnFirstThread \
     -cp target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q | tail -n 1) \
     com.ensea_java_final.Main