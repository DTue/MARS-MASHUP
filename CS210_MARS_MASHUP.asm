#To use: Navigate to the language switcher option under tools and select the most updated Music assmebly language(Music_(Ver_2))
#To test features: comment out by removing '#' in front of the instructions located below the features you want to test

#Feature 1: Play random chrismas music
xmas

play: 
bsti $t5, $t5, 1
#Feature 2: Asks dj for a random track
dj
dsn $t6, $t5, play

#Feature 3: Blend two registers together to get a new tune(value) - Find an average value between two registers
bsti $t0, $zero, 10
bsti $t1, $zero, 20
bl $t2, $t0, $t1
#dsn $t1, $t0, play

#Feature 4: Crossfade - cross/swap two registers 
cf $t1, $t2

#Feature 5: SHHH: mute all of your registers by setting all to 0
#mut $t0, $t1, $t2

#Feature 6: Get your notes/registers value
#dcy $t2, $t1
#flt $t3
#shrp $t4


#Feature 7: Sync your registers: set values the same
sync $t0, $t1, $t2

#Feature 8: Unlimited Play until program crash =)
#h play




