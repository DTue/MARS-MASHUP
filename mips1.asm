.data
track1: .asciiz "Track 1\n"
track2: .asciiz "Track 2\n"

msg_play: .asciiz "Play\n"
msg_pause: .asciiz "Pause\n"
msg_stop: .asciiz "Stop\n"

.text
bsti $t0, $zero, 10 #load 10 to 4t0
dsn $t0, $t1, play_track1
bsti $t1, $t0, 1

play_track1:

intro:
cr $t3, $t2 #increment value of $t2 and save $t3

#print play
bsti $v0, $zero, 4
la $a0, msg_play 
release

#print track 1
bsti $v0, $zero, 4
la $a0, track1
release

bsti $v0, $zero, 4
la $a0, msg_stop
release

#print track 2
bsti $v0, $zero, 4
la $a0, track2
release

outro:
dc $t3, $t2  #decrement value of $t2 and save $t3
Mut #silence everything - set all registers to 0











