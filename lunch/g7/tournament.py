import os
import _thread
import subprocess
import sys, time
from subprocess import Popen, list2cmdline, call
from itertools import permutations 
from itertools import combinations 
from copy import deepcopy

# Permutation
n_people = 4
players = ['g7','g1','g2','g3','g4','g5','g6','g8']


comb = combinations(players, n_people)
combation = [] 
for c in list(comb):
    if 'g7' in c:
        combation.append(c)
        new_c = tuple(reversed(c))
        combation.append(new_c)

# Monkey Count
n_monkeys = [10,30]
# Geese Count
n_geese = [10,30]
commands = []

for m in n_monkeys:
    for g in n_geese:
        log_filename = 'm=' + str(m) + '_and_' + 'g=' + str(g)
        for c in combation:
            cmd = 'java lunch.sim.Simulator -t 3600 --players '
            team = ' '.join(c)
            team = team.strip()
            cmd = cmd + team + ' -m ' + str(m) + ' -g ' + str(g) + ' -f ' + str(n_people) + ' -s 42 -l ' + log_filename
            commands.append(cmd)
            print(cmd)


def cpu_count():
    ''' Returns the number of CPUs in the system
    '''
    num = 1
    if sys.platform == 'win32':
        try:
            num = int(os.environ['NUMBER_OF_PROCESSORS'])
        except (ValueError, KeyError):
            pass
    elif sys.platform == 'darwin':
        try:
            num = int(os.popen('sysctl -n hw.ncpu').read())
        except ValueError:
            pass
    else:
        try:
            num = os.sysconf('SC_NPROCESSORS_ONLN')
        except (ValueError, OSError, AttributeError):
            pass

    return num

def exec_commands(cmds):
    ''' Exec commands in parallel in multiple process 
    (as much as we have CPU)
    '''
    if not cmds: return # empty list

    def done(p):
        return p.poll() is not None
    def success(p):
        return p.returncode == 0
    def fail():
        sys.exit(1)

    max_task = cpu_count()
    processes = []
    while True:
        while cmds and len(processes) < max_task:
            task = cmds.pop()
            print(task)
            processes.append(Popen(task,shell=True))

        for p in processes:
            if done(p):
                if success(p):
                    processes.remove(p)
                else:
                    fail()

        if not processes and not cmds:
            break
        else:
            time.sleep(0.05)

exec_commands(commands)
	
