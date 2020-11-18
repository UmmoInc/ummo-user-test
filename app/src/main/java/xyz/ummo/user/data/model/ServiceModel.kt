package xyz.ummo.user.data.model

interface ServiceModel {
    var serviceId: String? //0
    var serviceName: String? //1
    var serviceDescription: String? //2
    var serviceEligibility: String? //3
    var serviceCentres: ArrayList<String>? //4
    var presenceRequired: Boolean? //5
    var serviceCost: Int? //6
    var serviceDocuments: ArrayList<String>? //7
    var serviceDuration: String? //8
    var disapprovalCount: Int? //9
    var approvalCount: Int? //10
    var comments: ArrayList<String>? //11
    var serviceShares: Int? //12
    var serviceViews: Int? //13
    var serviceProvider: String? //14
}