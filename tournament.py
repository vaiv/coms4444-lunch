"""
This script will generate all combinatons of length familySize from the list of players.

It will then have each combination play games with monkeys and geese ranging from
minMonkey to maxMonkey and minGeese to maxGeese

It stores every result in a dataframe and keeps a list of (players, dataframe)

It's probably best to run this in an interactive setting like a jupyter notebook or using -i on python. 
"""


import subprocess, sys, os, glob, re, itertools
import pandas as pd

#parameters
players = ['g2','g4','g5','g7','g8']
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

allPossibleFamilies = [list(x) for x in itertools.combinations(players, familySize)]
allPossibleFamilies.extend([[x]*familySize for x in players])
print(allPossibleFamilies)

def runGames(team):
    results = pd.DataFrame(index = list(range(minGeese, maxGeese+1)), columns = list(range(minMonkey, maxMonkey+1)))
    for m in range(minMonkey, maxMonkey+1):
        for g in range(minGeese, maxGeese+1):
            try:
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
            except:
                print("unexpected result for ", " ".join(process))
                print(processResult)

    if(legibleOutput):
        results.index.name = "geese"
        results = results.add_suffix(' monkeys')

    return results

results = [ (x, runGames(x)) for x in allPossibleFamilies ]
