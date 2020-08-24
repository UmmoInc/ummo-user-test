package xyz.ummo.user.data.model

interface ServiceProviderModel {
    val serviceProviderId: String?
    val serviceProviderName: String?
    val serviceProviderProvince: String?
    val serviceProviderMunicipality: String?
    //    ArrayList<String> getServiceProviderLocation();
    val serviceProviderTown: String?
}