#!/bin/bash
cd IRCBouncer
mvn install
cd ..
mvn clean install -B