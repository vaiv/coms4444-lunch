run:
	javac lunch/*/*.java
	java lunch.sim.Simulator -t 6000 --players g4 g4 g4 g4 g4 g4 g4 g4 -m 30 -g 25 -f 8 -s 12328 -l log.txt

gui:
	javac lunch/*/*.java
	java lunch.sim.Simulator -t 7200 --players g4 g4 g4 g4 g4 g4 g4 g4 -m 30 -g 25 -f 8 -s 12328 --fps 1000 --gui -l log.txt

compile:
	javac lunch/sim/*.java
	javac lunch/g8/*.java

clean:
	rm lunch/*/*.class
