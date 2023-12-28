.data
t10: .word 0
t9: .word 0
t8: .word 0
t7: .word 0
res: .word 0
num: .word 0
prev: .word 0
count: .word 0
prevPrev: .word 0
a: .word 0
.text
main:
li $t1, 6
add $a0, $zero , $t1
jal fb
add $t2, $zero , $v0
sw $t2, a($zero)
j END_PROGRAM
fb:
sw $a0, num($zero)
li $t3, 1
sw $t3, prev($zero)
li $t4, 1
sw $t4, count($zero)
WHILE_LABEL_1_INIT:
lw $t8, count($zero)
lw $t9, num($zero)
slt $t5, $t8, $t9
add $t8, $zero, $t5
beq $zero, $t8, WHILE_LABEL_1_END
while:
lw $t8, prevPrev($zero)
lw $t9, prev($zero)
add $t6,$t8, $t9
sw $t6, res($zero)
lw $t7, prev($zero)
sw $t7, t7($zero)
lw $t7, t7($zero)
sw $t7, prevPrev($zero)
lw $t7, res($zero)
sw $t7, t8($zero)
lw $t7, t8($zero)
sw $t7, prev($zero)
lw $t8, count($zero)
li $t9, 1
lw $t7, t9($zero)
add $t7,$t8, $t9
sw $t7, t9($zero)
lw $t7, t9($zero)
sw $t7, count($zero)
j WHILE_LABEL_1_INIT
WHILE_LABEL_1_END:
lw $t7, res($zero)
sw $t7, t10($zero)
lw $t7, t10($zero)
add $v0, $zero , $t7
sw $t7, t10($zero)
jr $ra
END_PROGRAM:
