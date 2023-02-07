package com.letsclimbthis.esigtesttask.ui.certificates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letsclimbthis.esigtesttask.domain.signature.CertStoreUtil
import com.letsclimbthis.esigtesttask.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.*

class ViewModelCertificates : ViewModel() {

    private val _certificateList = mutableListOf<X509Certificate>()
    val certificateList: MutableList<X509Certificate> = Collections.unmodifiableList(_certificateList)

    private val _uiState = MutableLiveData<CertificatesUiState>()
    val uiState = _uiState as LiveData<CertificatesUiState>

    fun loadCertificateList() {
        viewModelScope.launch(Dispatchers.IO) {
            val listLoading = async { CertStoreUtil.loadTrustCertStoreCertificates() }
//            val listLoading = async { CertStoreUtil.loadCommonCertStoreCertificates() }
            val list = listLoading.await()
            withContext(Dispatchers.Main) {
                _certificateList.addAll(list)
                _uiState.value = CertificatesUiState.CertificateListLoaded
            }
        }
    }

    fun addCertificate(path: String) {
        val certFile = File(path)
        if (!certFile.exists()) throw IOException("There is no file at $path")
        viewModelScope.launch(Dispatchers.IO) {
           try {
               val certAdding = async { CertStoreUtil.saveCertificateToAppTrustStore(certFile) }
               val cert = certAdding.await()
               withContext(Dispatchers.Main) {
                   if (cert != null) {
                       // TODO: check that cert was actually added before updating the list
                       _certificateList.add(cert)
                       _uiState.value = CertificatesUiState
                           .CertificateAdded(_certificateList.size - 1)
                   } else {
                       _uiState.value = CertificatesUiState
                           .Failed("Error occurred while adding certificate")
                   }
               }
           } catch (e: Exception) {
               log("ViewModelCertificates: $e")
           }
        }
    }

    fun deleteCertificate(indexInList: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.IO) {
                val deleting = launch {
                    TODO("Not yet implemented")
                }
                deleting.join()
                withContext(Dispatchers.Main) {
                    _certificateList.removeAt(indexInList)
                    _uiState.value = CertificatesUiState.CertificateDeleted(indexInList)
                }
            }
        }
    }

}