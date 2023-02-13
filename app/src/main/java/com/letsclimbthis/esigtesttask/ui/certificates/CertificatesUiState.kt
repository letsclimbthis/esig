package com.letsclimbthis.esigtesttask.ui.certificates

sealed class CertificatesUiState(open val pageIndex: Int) {
    data class CertificateListLoaded(override val pageIndex: Int) : CertificatesUiState(pageIndex)
    data class CertificateAdded(override val pageIndex: Int, val newIndex: Int) : CertificatesUiState(pageIndex)
    data class CertificateDeleted(override val pageIndex: Int, val removedIndex: Int) : CertificatesUiState(pageIndex)
    data class Failed(override val pageIndex: Int, val message: String) : CertificatesUiState(pageIndex)
}
