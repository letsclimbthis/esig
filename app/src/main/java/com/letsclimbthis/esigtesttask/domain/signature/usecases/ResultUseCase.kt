package com.letsclimbthis.esigtesttask.domain.signature.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class ResultUseCase<Type, in Params> {

    protected abstract val workDispatcher: CoroutineDispatcher

    abstract suspend fun run(params: Params): Result<Type>

    suspend operator fun invoke(params: Params): Result<Type> = withContext(workDispatcher) {
        run(params)
    }

}