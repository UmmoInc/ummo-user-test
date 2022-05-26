package xyz.ummo.user.ui.detailedService

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import timber.log.Timber
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.data.repo.AppRepository

class DetailedProductViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository: AppRepository = AppRepository(application)
    private val productEntityLiveData: LiveData<ProductEntity>

    fun insertProduct(productEntity: ProductEntity) {
        appRepository.insertProduct(productEntity)
        Timber.e("insertProduct: PRODUCT->%s", productEntity.productId)
    }

    fun updateProduct(productEntity: ProductEntity?) {
        appRepository.updateProduct(productEntity)
    }

    fun deleteAllProduct() {
        appRepository.deleteAllProducts()
    }

    fun getProductEntityLiveDataById(productId: String?): LiveData<ProductEntity> {
        return appRepository.getProductEntityLiveDataById(productId)
    }

    fun getDelegatedProduct(isDelegated: Boolean?): LiveData<ProductEntity> {
        return appRepository.getDelegatedProduct(isDelegated)
    }

    init {
        productEntityLiveData = appRepository.productEntityLiveData
    }
}