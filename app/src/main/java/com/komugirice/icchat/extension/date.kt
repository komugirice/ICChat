package com.komugirice.icchat.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.getDateToString(): String =
    SimpleDateFormat("yyyy/MM/dd").format(this)

fun Date.getJPDateToString(): String =
    SimpleDateFormat("yyyy年MM月dd日").format(this)

fun Date.yyyyMMddHHmmToString(): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm").format(this)

fun Date.HHmmToString(): String =
    SimpleDateFormat("HH:mm").format(this)

fun Date.compareDate(target: Date): Boolean =
    this.getDateToString().equals(target.getDateToString())

