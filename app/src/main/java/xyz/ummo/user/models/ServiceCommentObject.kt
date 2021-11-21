package xyz.ummo.user.models

import java.io.Serializable

data class ServiceCommentObject(
    var serviceId: String,
    var serviceComment: String,
    var commentDateTime: String,
    var userName: String
) : Serializable