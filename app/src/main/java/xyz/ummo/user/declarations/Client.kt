package xyz.ummo.user.declarations

import io.socket.client.Manager
import io.socket.client.Socket

/*
* So here we are trying to extend this class so that we can seperate the init process into its
* constructor then use it going forward from there
* */
class Client : Socket {
    // this is the services object that will have all the different classes of handlers.
    private val services: LinkedHashMap<String, Any> = LinkedHashMap()

    // though the constructor is not explicitly used i will keep it here for my readability sake.
    constructor(io: Manager?, nsp: String?, opts: Manager.Options?) : super(io, nsp, opts) {
        /*
        * So here since the super constructor is called we should be able to just connect from here
        * */
    }

    /*
    * to bind services we are going to need to store them in some sort of key map object then maybe
    * define a set of functions to set and get the services inside of the map.
    * */

    // so we define the function to bind to the map of services
    fun bind(service: Any) {

    }

    // then we define the function to get the services.
    fun fetch(name: String) {
        // we use a string assuming that the maps keys will be of type string.
    }
}