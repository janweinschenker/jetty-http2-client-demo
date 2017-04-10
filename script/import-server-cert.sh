#!/bin/bash
filename=$1

keytool -import -v -trustcacerts -alias localhost-alias2 -file $filename -keystore $JAVA_HOME/jre/lib/security/cacerts -keypass changeit -storepass changeit
