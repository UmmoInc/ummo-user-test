package xyz.ummo.user.models

import java.io.Serializable

data class ServiceBenefit(
    val benefitTitle: String,
    val benefitBody: String
) : Serializable
