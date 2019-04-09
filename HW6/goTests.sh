##!/usr/bin/env bash
#prefix=/Users/elena/Desktop/JavaAdvanced/HW6
#tests=%prefix%artifacts
#libs=%prefix%lib
#pack=ru/ifmo/rain/ilina/implementor/
#compiled=/Users/elena/Desktop/JavaAdvanced/HW6/out/production/HW6
#src=/Users/elena/Desktop/JavaAdvanced/HW6/src/%pack%
#jar=%tests%\info.kgeorgiy.java.advanced.implementor.jar
#javac -d %compiled% -cp %jar% %src%Implementor.java
#classpath=%libs%;%tests%;%compiled%
#java  -Dfile.encoding=UTF-8 -p %classpath% -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.ilina.implementor.Implementor
java -cp ./out/production/HW6 -p ./lib:./artifacts -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.ilina.implementor.Implementor 5C
