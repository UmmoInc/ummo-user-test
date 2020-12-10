package xyz.ummo.user.data.model

interface ServiceModel {
    var serviceId: String? //0
    var serviceName: String? //1
    var serviceDescription: String? //2
    var serviceEligibility: String? //3
    var serviceCentres: ArrayList<String>? //4
    //    var serviceCentres: String?
    var presenceRequired: Boolean? //5
    var serviceCost: String? //6
    var serviceDocuments: ArrayList<String>? //7
    var serviceDuration: String? //8
    var notUsefulCount: Int? //9
    var usefulCount: Int? //10
    var serviceComments: ArrayList<String>? //11
    var commentCount: Int? //12
    var serviceShares: Int? //13
    var serviceViews: Int? //14
    var serviceProvider: String? //15
    var bookmarked: Boolean? //16
    var isDelegated: Boolean? //17
}