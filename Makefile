run:
	java lunch.sim.Simulator -t 3600 --players random -m 1 -g 1 -f 1 -s 42 -l log.txt

gui:
	java lunch.sim.Simulator -t 3600 --players random random random random -m 10 -g 6 -f 4 -s 42 --fps 10 --gui -l log.txt

compile:
	javac lunch/sim/*.java
	javac lunch/g8/*.java

clean:
	rm lunch/*/*.class
