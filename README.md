# power-readings-analyzer

Analyzer which determines whether or not the device reacts quickly enough to meet National Grid requirements for Fast Frequency Response (FFR). 
In the FFR programme, assets that consume power automatically switch off (or "turn down") to reduce power consumption when there is an under-supply. 
The control signal is the supply frequency: when the frequency drops below a threshold, there is an under-supply of power on the grid.


It takes the example set of meter readings as input, to determine:
How long it took for the relay to switch
How long it took for the device to turn down after the relay switched
Whether or not the device passed the test

## Execution

In order to run it use next command as example:

`java -jar power-readings-analyzer_2.12-1.0-one-jar.jar "/home/andrei/Projects/github/andrei-l/power-readings-analyzer/readings.csv"`

Project has a number of unit tests. In order to run tests use:

`sbt test`

In order to build a runnable jar use:

`sbt oneJar`
