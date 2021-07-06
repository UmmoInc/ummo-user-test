package xyz.ummo.user.models

import java.io.Serializable

data class ServiceProviderData(val serviceProviderId: String, //0
                               val serviceProviderName: String, //1
                               val serviceProviderDescription: String, //2
                               val serviceProviderContact: String, //3
                               val serviceProviderEmail: String, //4
                               val serviceProviderAddress: String) //5
    : Serializable