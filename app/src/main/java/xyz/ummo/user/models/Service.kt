package xyz.ummo.user.models

data class Service(val serviceName: String, //1
                   val serviceDescription: String, //2
                   val serviceEligibility: String, //3
                   val serviceCentre: String, //4
                   val serviceCost: String, //5
                   val serviceRequirements: String, //6 TODO: upgrade to Array
                   val serviceDuration: String, //7
                   val approveCount: Int, //8
                   val disapproveCount: Int, //9
                   val commentCount: Int, //10 TODO: upgrade to Array
                   val shares: Int, //11
                   val views: Int) //12