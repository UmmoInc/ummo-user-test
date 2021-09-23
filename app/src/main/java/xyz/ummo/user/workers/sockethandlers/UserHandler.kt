package xyz.ummo.user.workers.sockethandlers

import io.socket.client.Socket
import timber.log.Timber

class UserHandler(socket: Socket) {

    val name = "user"

    init {
        socket.on("$name/notify") {
            this.notify(it.toString())
        }
    }

    private fun notify(message: String) {
        Timber.e("USER MESSAGE -> $message")
    }
}