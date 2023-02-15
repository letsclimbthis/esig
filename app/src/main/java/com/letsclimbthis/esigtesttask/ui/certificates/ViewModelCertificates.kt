package com.letsclimbthis.esigtesttask.ui.certificates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letsclimbthis.esigtesttask.domain.signature.CertStoreUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.cert.X509Certificate
import java.util.*

class ViewModelCertificates : ViewModel() {

    private val _uiState = MutableLiveData<CertificatesUiState>()
    val uiState = _uiState as LiveData<CertificatesUiState>

    private val _rootCertificateList = mutableListOf<X509Certificate>()
    val rootCertificateList: List<X509Certificate> = Collections.unmodifiableList(_rootCertificateList)

    private val _commonCertificateList = mutableListOf<X509Certificate>()
    val commonCertificateList: List<X509Certificate> = Collections.unmodifiableList(_commonCertificateList)

    // used by viewpager to set title for each page
    private val certificateListsNames = listOf(
        "Root",
        "Intermediate"
    )

    // used by viewpager to resolve amount of pages
    val amountOfCertificateLists = certificateListsNames.size

    init {
        loadRootCertificates()
        loadCommonCertificates()
    }

    fun getCertificateListName(index: Int): String {
        return certificateListsNames[index]
    }

    fun getCertificateList(pageIndex: Int): List<X509Certificate> {
        return when(pageIndex) {
            0 -> rootCertificateList
            1 -> commonCertificateList
            else -> emptyList()
        }
    }

    fun addCertificate(pageIndex: Int, path: String) {
        when (pageIndex) {
            0 ->
                addCertificate(
                    path,
                    CertStoreUtil::saveCertificateToRootCertStore,
                    CertStoreUtil::isCertificateInRootCertStore,
                    pageIndex,
                    _rootCertificateList
                )
            1 ->
                addCertificate(
                    path,
                    CertStoreUtil::saveCertificateToCommonCertStore,
                    CertStoreUtil::isCertificateInCommonCertStore,
                    pageIndex,
                    _commonCertificateList
                )
            else -> {}
        }
    }

    fun deleteCertificate(pageIndex: Int, indexInList: Int) {
        when (pageIndex) {
            0 -> {
                deleteCertificate(
                    indexInList,
                    CertStoreUtil::deleteCertificateFromRootCertStore,
                    CertStoreUtil::isCertificateInRootCertStore,
                    pageIndex,
                    _rootCertificateList
                )
            }
            1 -> {
                deleteCertificate(
                    indexInList,
                    CertStoreUtil::deleteCertificateFromCommonCertStore,
                    CertStoreUtil::isCertificateInCommonCertStore,
                    pageIndex,
                    _commonCertificateList
                )
            }
            else -> {}
        }
    }

    private fun addCertificate(
        path: String,
        addFunc: (File) -> X509Certificate?,
        checkFunc: (X509Certificate) -> Boolean,
        pageIndex: Int,
        list: MutableList<X509Certificate>
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val adding = async {
                addFunc(File(path))
            }
            val certificate = adding.await()

            if (certificate == null || !checkFunc(certificate)) {
                withContext(Dispatchers.Main) {
                    _uiState.value = CertificatesUiState.Failed(
                        pageIndex,
                        "An error occurred while adding certificate"
                    )
                }

            } else {
                withContext(Dispatchers.Main) {
                    list.add(certificate)
                    _uiState.value = CertificatesUiState.CertificateAdded(
                        pageIndex,
                        list.size - 1
                    )
                }
            }
        }
    }


    private fun deleteCertificate(
        indexInList: Int,
        deleteFunc: (X509Certificate) -> Unit,
        checkFunc: (X509Certificate) -> Boolean,
        pageIndex: Int,
        list: MutableList<X509Certificate>
    ) {
        val certificate = list[indexInList]

        viewModelScope.launch(Dispatchers.IO) {
            val deleting = launch {
                deleteFunc(certificate)
            }
            deleting.join()

            if (checkFunc(certificate)) {
                withContext(Dispatchers.Main) {
                    _uiState.value = CertificatesUiState.Failed(
                        pageIndex,
                        "An error occurred while deleting certificate"
                    )
                }

            } else {
                withContext(Dispatchers.Main) {
                    list.removeAt(indexInList)
                    _uiState.value = CertificatesUiState.CertificateDeleted(pageIndex, indexInList)
                }
            }
        }
    }

    private fun loadRootCertificates() {
        viewModelScope.launch(Dispatchers.IO) {
            val listLoading = async { CertStoreUtil.loadRootCertStoreCertificates() }
            val list = listLoading.await()

            withContext(Dispatchers.Main) {
                _rootCertificateList
                    .addAll(list)
                _uiState.value = CertificatesUiState.CertificateListLoaded(0)
            }
        }
    }

    private fun loadCommonCertificates() {
        viewModelScope.launch(Dispatchers.IO) {
            val listLoading = async { CertStoreUtil.loadCommonCertStoreCertificates() }
            val list = listLoading.await()

            withContext(Dispatchers.Main) {
                _commonCertificateList
                    .addAll(list)
                _uiState.value = CertificatesUiState.CertificateListLoaded(1)
            }
        }
    }

    private val className = "ViewModelCertificates"

}