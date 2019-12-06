import os
import thread
import subprocess
import sys, time
from subprocess import Popen, list2cmdline, call
from itertools import combinations

players = ['g1','g2','g3','g4','g5','g6','g7','g8','random']
geese = [0,100,30,5,15]
monkeys = [300,0,15,30,100]
time_limit = [10]
reps = [1,2,3,4,5]
k_list = [1,2,3,4,5,6,7,8,9]

commands = []
run = 0
for k in k_list:
    combs = combinations(players,k)
    for comb in combs:
        f = len(comb)
        for g in geese:
            for m in monkeys:
                for t in time_limit:
                    run+=1
                    path ='tournament_logs/run_' +str(run)
                    os.system('mkdir -p '+path)
                    for r in reps:
                        log_file = 'run_' + str(r) + '.txt'
                        log_file = os.path.join(path,log_file)
                        player_list = ''
                        for player in comb:
                            player_list += ' ' + player
                        cmd = 'java lunch.sim.Simulator -t ' + str(t*60) + ' --players ' + player_list
                        cmd+= ' -m ' + str(m) + ' -g '+ str(g) + ' -f '+str(f) + ' -s '+ str(42+r) + '  -l ' + log_file
			cmd+= ' > /dev/null'
                        print(cmd)
                        commands.append(cmd)

                        
                        



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
	
