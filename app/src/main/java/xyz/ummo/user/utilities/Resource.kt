package xyz.ummo.user.utilities

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T?) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}