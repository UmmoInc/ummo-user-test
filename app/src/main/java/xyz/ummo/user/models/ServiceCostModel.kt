package xyz.ummo.user.models

import java.io.Serializable

data class ServiceCostModel (val serviceSpec: String, val specCost: Int): Serializable {

    private val serviceCostBySpec = "$serviceSpec: E$specCost"
    override fun toString(): String {
        return serviceCostBySpec
    }
}