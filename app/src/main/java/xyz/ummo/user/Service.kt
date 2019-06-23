package xyz.ummo.user

class Service(val serviceName: String, val serviceDescription: String, val form: String, val personalDocs: String,
              val cost: String, val duraion: String, private var steps: List<String>?) {

    fun setSteps(steps: Array<String>) {
        this.steps = steps
    }

    fun getSteps(): Array<String>? {
        return steps
    }
}
