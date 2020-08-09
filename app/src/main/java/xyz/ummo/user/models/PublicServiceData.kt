package xyz.ummo.user.models

import xyz.ummo.user.Product

data class PublicServiceData(val serviceName: String,
                             val province: String,
                             val municipality: String,
                             val town: String,
                             val serviceCode: String)