package xyz.ummo.user.models

import java.io.Serializable

data class ServiceMiniObject_depr(
    var serviceId: String,
    var serviceName: String,
    var serviceDescription: String
) : Serializable