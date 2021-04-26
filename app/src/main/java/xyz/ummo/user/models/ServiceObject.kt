package xyz.ummo.user.models

import org.json.JSONObject
import java.io.Serializable

data class ServiceObject(var serviceId: String, //0
                         var serviceName: String, //1
                         var serviceDescription: String, //2
                         var serviceEligibility: String, //3
                         var serviceCentres: ArrayList<String>, //4
                         var delegatable: Boolean, //5
                         var serviceCost: ArrayList<ServiceCostModel>, //6
                         var serviceDocuments: ArrayList<String>, //7
                         var serviceDuration: String, //8
                         var usefulCount: Int, //9
                         var notUsefulCount: Int, //10
                         var serviceComments: ArrayList<String>, //11
                         var serviceCommentCount: Int, //12
                         var serviceShareCount: Int, //13
                         var serviceViewCount: Int, //14
                         var serviceProvider: String, //15
                         var serviceLink: String, //16
                         var serviceAttachmentName: String,
                         var serviceAttachmentSize: String,
                         var serviceAttachmentURL: String) //17
    : Serializable