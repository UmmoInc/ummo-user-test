package xyz.ummo.user.delegate

abstract class DelegateService(user:String,agent:String,fare_rate:String,product:String) {
    abstract fun done()
}