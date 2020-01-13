package com.komugirice.icchat.enums

enum class MessageType(val id: Int) {
    TEXT(0),
    IMAGE(1),
    FILE(2),
    QUOTE(3),
    SYSTEM(4);

    companion object {

        fun getValue(type: Int): MessageType {
            return values().firstOrNull {it.id == type} ?: TEXT
        }
    }
    val isText: Boolean
    get() = this == TEXT

    val isIMAGE: Boolean
    get() = this == IMAGE

    val isFILE: Boolean
    get() = this == FILE

    val isQUOTE: Boolean
    get() = this == QUOTE

    val isSYSTEM: Boolean
    get() = this == SYSTEM
}