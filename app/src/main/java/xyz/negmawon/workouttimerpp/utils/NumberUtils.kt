package xyz.negmawon.workouttimerpp.utils

fun totalSecond(min: Int, sec: Int) = min * 60 + sec

fun Int.twoDigitString() = this.toString().padStart(2, '0')

