# Project 4: Lunch
Course: COMS 4444 Programming and Problem Solving (F2019)  
Uni: Columbia University  
Instructor: Prof. Kenneth Ross   
TAs: Vaibhav Darbari, Chengyu Lin   

### Steps to run the simulator:
1. `make compile`
2. Update the make file with participating groups.
3. `make run` OR `make gui

### `Simulator Arguments
-m or --num_monkeys : number of monkeys.

-g or --num_geese: number of geese.

-f : number of family members.

-p or --players : space separated players.

-t or --simulation_time: time for which the simulation runs.

-s or --seed: seed value for random.

-l or --log : enable logging

-v or --verbose : whether a verbose log should be recorded for the games when logging is enabled.

--gui: enable gui

--fps : fps


This project belongs to Columbia University. It may be freely used for educational purposes.

### Description of Important Classes
#### 1) Player: class to be implemented by your player.
##### Important methods in player class:
```init```: return a string which represents family to be displayed(for default return empty string). The avatar PNGs should be added to the statics folder and should be named accprding to convention: avatar_1.png, avatar_2.png, etc. <br/>
```getCommand```: return the command to ne executed for this round.

#### 2) Family: Contains properties of other family members.
##### Important methods in Family class:
```get_held_item_type```: get the type of food held by the family member. <br/>
```get_location```: get the location of family member.

#### 3) Animal: Contains properties of Animals.
##### Important methods in Animal class:
```get_location```: get the location of animal. <br/>
```which_animal```: get the type of animal. <br/>
```get_max_speed```: get the max speed of the animal. <br/>
```busy_eating```: check if animal is busy eating something. <br/>

#### 4) PlayerState: Contains player's properties.
##### Important methods in PlayerState class:
```time_to_eat_remaining```: time remaining for item in hand to be fully consumed. <br/>
```is_player_searching```: check if player is busy looking inside the bag. <br/>
```time_to_finish_search```: get the time remaining in finishing search inside the bag. <br/>
```get_time_for_item```: get the time remaining for item specified to be fully consumed. <br/>
```check_availability_item```: check if the specified item is available for consumption. <br/>

#### 5) Command: Specified command structure
##### 6) Important methods in Command class:
```createMoveCommand```: create a MOVE_TO command by specifying a location within bounds. <br/>
```createRetrieveCommand```: create a TAKE_OUT command by specifying the type of food to be taken out. <br/>

#### 7) CommandType: Specifies the types of command

#### 8) FoodType: Specifies the types of food items.

