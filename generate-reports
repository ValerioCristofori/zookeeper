#!/bin/bash

#generate jacoco report
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package

#generate pit report
mvn -DwithHistory org.pitest:pitest-maven:mutationCoverage surefire:test -Ppit
