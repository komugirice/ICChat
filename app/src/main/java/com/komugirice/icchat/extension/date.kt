package com.example.qiitaapplication.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.getDateToString(): String =
    SimpleDateFormat("yyyy/MM/dd").format(this)

fun Date.yyyyMMddHHmmToString(): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm").format(this)

fun Date.HHmmToString(): String =
    SimpleDateFormat("HH:mm").format(this)

fun Date.compareDate(target: Date): Boolean =
    if (this.getDateToString().equals(target.getDateToString()))
        true
    else
        false

