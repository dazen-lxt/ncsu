package com.ncsu.imagc.extensions

fun Double.toString(decimals: Int): String {
    return String.format("%.${decimals}f", this)
}