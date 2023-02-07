package com.letsclimbthis.esigtesttask.ui.certificates

sealed class CertificatesUiState {
    object CertificateListLoaded : CertificatesUiState()
    //    data class CertificateListLoaded(val newIndices: Flow<Int>) : CertificatesUiState()
    data class CertificateAdded(val newIndex: Int) : CertificatesUiState()
    data class CertificateDeleted(val removedIndex: Int) : CertificatesUiState()
    data class Failed(val message: String) : CertificatesUiState()
}
