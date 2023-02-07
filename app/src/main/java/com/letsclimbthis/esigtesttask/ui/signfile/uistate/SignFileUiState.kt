package com.letsclimbthis.esigtesttask.ui.signfile.uistate

data class SignFileUiState(
    val fileState: FileState,
    val keyContainerState: KeyContainerState,
    val signatureState: SignatureState,
    val message: String
)
