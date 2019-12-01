import subprocess, sys, os, glob, re
import pandas as pd

#parameters
mainPlayer = 'g4'
familySize = 4
time = 3600
minMonkey = 1
maxMonkey = 2
minGeese = 1
maxGeese = 2
seed = 12345
legibleOutput = False #disable this to have more machine readable output
logPath = './logs'
if not os.path.exists(logPath):
    os.makedirs(logPath)


def runGames(team):
    results = pd.DataFrame(index = list(range(minGeese, maxGeese+1)), columns = list(range(minMonkey, maxMonkey+1)))
    for m in range(minMonkey, maxMonkey+1):
        for g in range(minGeese, maxGeese+1):
            process = ["java", "lunch.sim.Simulator", "-t", str(time), "--players"]
            process.extend(team)
            process.extend(["-m",str(m),"-g",str(g),"-f", str(familySize), "-s", str(seed), "-l" ,f'{logPath}/log_{mainPlayer}_{m}_{g}.txt'])
            print(" ".join(process))
            processResult = subprocess.check_output(process)
            processResult = processResult.decode('utf-8')
            ri = processResult.index('----------------------------------------------Summary of results------------------------------------------\n\n')
            scoresheet = processResult[ri:]
            scores = scoresheet.split('\n\n')
            scores = scores[2:]
            scores = scores[:-2]
            numericals = [int(x.split('\t\t')[1]) for x in scores]
            totalScore = sum(numericals)
            print(f'Team : {mainPlayer}, m: {m}, g: {g}, score: {totalScore}')
            results[g][m] = totalScore

    if(legibleOutput):
        results.index.name = "geese"
        results = results.add_suffix(' monkeys')

    return results

results = runGames([mainPlayer]*familySize)
