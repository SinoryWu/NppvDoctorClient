package com.hzdq.viewmodelshare

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class VMScope(val scopeName:String) {
}