package com.komugirice.icchat.enums

enum class MessageType(val id: Int) {
    TEXT(0),
    IMAGE(1),
    FILE(2),
    QUOTE(3),
    SYSTEM(4),
    DATE(5);

    companion object {

        fun getValue(type: Int): MessageType {
            return values().firstOrNull {it.id == type} ?: TEXT
        }
    }
    val isText: Boolean
    get() = this == TEXT

    val isImage: Boolean
    get() = this == IMAGE

    val isFile: Boolean
    get() = this == FILE

    val isQuote: Boolean
    get() = this == QUOTE

    val isSystem: Boolean
    get() = this == SYSTEM

    val isDate: Boolean
        get() = this == DATE
}