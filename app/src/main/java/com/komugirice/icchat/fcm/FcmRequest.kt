package com.komugirice.icchat.fcm

class FcmRequest {

    var to: String = "" // 送りたい相手のFcmToken

    var data = Data()


    class Data {
        var title = ""
        var message = ""
    }
}