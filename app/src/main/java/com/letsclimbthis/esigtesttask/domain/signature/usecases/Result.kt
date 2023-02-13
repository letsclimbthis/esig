package com.letsclimbthis.esigtesttask.domain.signature.usecases

sealed class Result<Type> {
    data class Success<Type>(val value: Type) : Result<Type>()
    data class Error(val message: String) : Result<String>()
}
