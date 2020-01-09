package com.komugirice.icchat.enum

enum class MessageType(val id: Int) {
    TEXT(0),
    IMAGE(1),
    FILE(2),
    QUOTE(3),
    SYSTEM(4)
}