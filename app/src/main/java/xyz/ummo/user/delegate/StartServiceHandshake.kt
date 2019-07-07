package xyz.ummo.user.delegate

abstract class StartServiceHandshake(serviceId:String,agentId:String,iserId:String) {

    init {
        handshakeFinished(true)
    }

    abstract fun handshakeFinished(accepted:Boolean)
}