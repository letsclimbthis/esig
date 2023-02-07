package com.letsclimbthis.esigtesttask.domain.signature

import java.security.KeyStore

interface Sig {

    fun sign(
        fileToSignPath: String,
        keyStore: KeyStore,
        containerAlias: String,
        cryptoProviderName: String,
    )
}