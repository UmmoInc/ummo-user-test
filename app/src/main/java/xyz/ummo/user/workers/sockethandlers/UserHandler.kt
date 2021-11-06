package xyz.ummo.user.workers.sockethandlers

import io.socket.client.Socket
import timber.log.Timber

class UserHandler(socket: Socket) {

    val name = "user"

    init {
        socket.on("$name/notify") {
            this.notify(it.toString())
            Timber.e("USER MESSAGE -> ${it[0]}")
        }

        socket.on("/user/verified") {
            this.notify(it.toString())
            Timber.e("EMAIL VERIFIER -> ${it[0]}")

        }
    }

    private fun notify(message: String) {
        Timber.e("USER MESSAGE -> $message")
    }

}