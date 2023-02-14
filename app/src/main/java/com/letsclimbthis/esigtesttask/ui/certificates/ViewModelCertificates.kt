package com.letsclimbthis.esigtesttask.ui.certificates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.letsclimbthis.esigtesttask.domain.signature.CertStoreUtil
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.utils.getSubjectName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
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
        loadCertListFromStore()
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
        viewModelScope.launch(Dispatchers.IO) {

            val currentList = when (pageIndex) {
                0 -> _rootCertificateList
                1 -> _commonCertificateList
                else -> null
            }

            currentList?.let { list ->
                viewModelScope.launch(Dispatchers.IO) {
                    val adding = async {
                        CertStoreUtil.saveCertificateToCertStore(
                            File(path)
                        )
                    }
                    val certificate = adding.await()

                    withContext(Dispatchers.Main) {
                        certificate?.let { cert ->
                            if (!CertStoreUtil.isCertificateInStore(cert)) {
                                _uiState.value = CertificatesUiState.Failed(
                                    pageIndex,
                                    "An error occurred while adding certificate"
                                )
                            } else {
                                list.add(cert)
                                _uiState.value = CertificatesUiState.CertificateAdded(
                                    pageIndex,
                                    list.size - 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteCertificate(pageIndex: Int, indexInList: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = when(pageIndex) {
                0 -> _rootCertificateList
                1 -> _commonCertificateList
                else -> null
            }

            currentList?.let {
                val certToDelete = it[indexInList]

                viewModelScope.launch(Dispatchers.IO) {
                    val deleting = launch {
                        CertStoreUtil.deleteCertificate(certToDelete)
                    }
                    deleting.join()

                    withContext(Dispatchers.Main) {
                        if (CertStoreUtil.isCertificateInStore(certToDelete)) {
                            _uiState.value = CertificatesUiState.Failed(
                                pageIndex,
                                "An error occurred while deleting certificate"
                            )
                        } else {
                            it.removeAt(indexInList)
                            _uiState.value = CertificatesUiState.CertificateDeleted(pageIndex, indexInList)
                        }
                    }
                }
            }
        }
    }

    private fun loadCertListFromStore() {
        viewModelScope.launch(Dispatchers.IO) {
            val listLoading = async { CertStoreUtil.loadCertificatesFromStoreByCategory() }
            // returns array of list: under index 0 - root certs, under index 1 - common certs
            val list = listLoading.await()

            withContext(Dispatchers.Main) {
                _rootCertificateList
                    .addAll(
                        list[0].filter { it.getSubjectName().isNotEmpty() }
                    )
                _uiState.value = CertificatesUiState.CertificateListLoaded(0)

                _commonCertificateList
                    .addAll(
                        list[1].filter { it.getSubjectName().isNotEmpty() }
                    )
                _uiState.value = CertificatesUiState.CertificateListLoaded(1)
            }
        }
    }

    private val className = "ViewModelCertificates"

}