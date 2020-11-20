package xyz.ummo.user.models

data class Service(var serviceId: String, //0
                   var serviceName: String, //1
                   var serviceDescription: String, //2
                   var serviceEligibility: String, //3
                   var serviceCentre: ArrayList<String>, //4
                   var presenceRequired: Boolean, //5
                   var serviceCost: String, //6
                   var serviceDocuments: ArrayList<String>, //7 TODO: upgrade to Array
                   var serviceDuration: String, //8
                   var usefulCount: Int, //9
                   var notUsefulCount: Int, //10
                   var commentCount: Int, //11 TODO: upgrade to Array
                   var shares: Int, //12
                   var views: Int, //13
                   var serviceProvider: String) //14