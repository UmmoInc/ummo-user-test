package xyz.ummo.user.workers.sockethandlers

import io.socket.client.Socket
import timber.log.Timber

class AgentHandler(socket: Socket) {

    val name = "agent"

    init {
        socket.on("$name/request") {
            this.notify(it.toString())
        }
    }

    private fun notify(message: String) {
        Timber.e("AGENT MESSAGE -> $message")
    }
}