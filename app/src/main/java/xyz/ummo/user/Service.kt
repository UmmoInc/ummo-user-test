package xyz.ummo.user

data class Service(val serviceName: String, val serviceDescription: String, val form: String, val personalDocs: String,
              val cost: String, val duraion: String, val steps: List<String>?)
