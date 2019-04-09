#!/usr/bin/env bash

proj=/Users/elena/Desktop/JavaAdvanced/HW6
prefix=/Users/elena/Desktop/JavaAdvanced/HW6
lib=${prefix}/lib
test=${prefix}/artifacts/info.kgeorgiy.java.advanced.implementor.jar
out=out/production/artifacts
compiled=${proj}/out/production/HW6
man=${proj}/src/Manifest.txt
dep=info/kgeorgiy/java/advanced/implementor/

cd ${proj}
sudo javac -d ${out} -cp ${lib};${test};src/ru/ifmo/rain/ilina/implementor/Implementor.java

#sudo mkdir ${out}
cd ${out}
sudo jar xf ${test} ${dep}Impler.class ${dep}JarImpler.class ${dep}ImplerException.class
sudo jar -cfm Implementor.jar ${man} ru/ifmo/rain/ilina/implementor/*.class ${dep}*.class
sudo rmdir info /s /q
cd ${proj}