run:
	java lunch.sim.Simulator -t 10000 --players random -m 1 -g 1 -f 1 -s 42 -l log.txt

gui:
	java lunch.sim.Simulator -t 10000 --players random random random random random random -m 15 -g 2 -f 6 -s 42 --fps 10 --gui -l log.txt

compile:
	javac lunch/sim/*.java

clean:
	rm lunch/*/*.class
