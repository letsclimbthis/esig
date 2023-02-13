package com.letsclimbthis.esigtesttask.ui.signfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letsclimbthis.esigtesttask.domain.signature.CSP
import com.letsclimbthis.esigtesttask.domain.signature.PKCS7
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.signfile.uistate.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.CryptoPro.JCSP.JCSP
import java.io.File
import java.security.cert.X509Certificate
import kotlin.contracts.contract
import kotlin.properties.Delegates

class ViewModelSignFile : ViewModel() {

    private val emptyMessage = ""

    private var typeOfKeyStorageToRead = "Aladdin R.D. JaCarta 00 00"

    // all the certificates read from key storage with corresponding aliases
    private lateinit var loadedListOfKeyContainers:  List<Pair<String, X509Certificate>>
    // index of chosen key container
    private var currentChosenKeyContainerIndex by Delegates.notNull<Int>()

    private val _uiState = MutableStateFlow(
        SignFileUiState(
            FileState.Initial,
            KeyContainerState.Initial,
            SignatureState.Initial,
            emptyMessage
        )
    )
    val uiState: StateFlow<SignFileUiState> = _uiState.asStateFlow()

    fun setChosenFileToSign(file: File) {
        _uiState.update {
            buildUiStateWithNewComponent(FileState.FileChosen(file), emptyMessage)
        }
    }

    fun cancelChoiceOfFile() = _uiState.update {
        buildUiStateWithNewComponent(FileState.FileNotChosen, emptyMessage)
    }

    fun setTypeOfKeyStorageToRead() {}

    fun loadKeyContainers() {
        _uiState.update {
            buildUiStateWithNewComponent(
                KeyContainerState.KeyContainersLoading,
                emptyMessage
            )
        }

        viewModelScope.launch(Dispatchers.IO) {

            val reading = async {
                CSP.getCertAndAliasFromKeyContainers()
            }

            val list = reading.await()

            // update state with key container aliases,
            // that were read from key storage,
            // or with error message
            withContext(Dispatchers.Main) {
                _uiState.update {
                    if (list.isNotEmpty()) {
                        loadedListOfKeyContainers = list
                        buildUiStateWithNewComponent(
                            KeyContainerState.KeyContainersLoaded(list.map { it.first }),
                            emptyMessage
                        )

                    } else
                        buildUiStateWithNewComponent(
                            KeyContainerState.KeyContainersLoadingFailed,
                            "There are no key containers"
                        )
                }
            }
        }
    }

    // update state with Certificate,
    // that was chosen by user clicking
    // on its key container alias
    fun setKeyContainer(index: Int) {
        currentChosenKeyContainerIndex = index
        _uiState.update {
            buildUiStateWithNewComponent(
                KeyContainerState.KeyContainerChosen(
                    loadedListOfKeyContainers[index]
                ),
                emptyMessage
            )
        }
    }

    fun signFile() {
        val filePath = "/storage/emulated/0/Sig/test.txt"
//        val filePath = "/storage/emulated/0/${(_uiState.value.fileState as FileState.FileChosen).file.path}"
        val keyContainerAlias = loadedListOfKeyContainers[currentChosenKeyContainerIndex].first

        log("filePath = $filePath")
        log("keyContainerAlias = $keyContainerAlias")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                PKCS7.createSign(
                    filePath,
                    typeOfKeyStorageToRead,
                    keyContainerAlias
                )
            }
            catch (e: Exception) {
                log(e)
            }
        }
    }

    fun deleteSignatureFile() {}

    private fun buildUiStateWithNewComponent(newStateComponent: StateComponent, message: String
    ): SignFileUiState {
        val currentState = _uiState.value
        return when (newStateComponent) {
            is FileState
            -> SignFileUiState(
                newStateComponent,
                currentState.keyContainerState,
                currentState.signatureState,
                message
            )
            is KeyContainerState
            -> SignFileUiState(
                currentState.fileState,
                newStateComponent,
                currentState.signatureState,
                message
            )
            is SignatureState
            -> SignFileUiState(
                currentState.fileState,
                currentState.keyContainerState,
                newStateComponent,
                message
            )
        }
    }

}





//        log("${file.absolutePath.substring(file.absolutePath.lastIndexOf("."))}")


//    private val _upd = MutableLiveData<String>().apply {
//        value = "This is home file viewer fragment"
//    }
//    val upd: LiveData<String> = _upd

//    private val _filesInDirectoryList = mutableListOf<File>()
//    val filesInDirectoryList: MutableList<File> = Collections.unmodifiableList(_filesInDirectoryList)

//    private val history = mutableListOf<File>()

//    fun updateDirectory(file: File) {
//        if (file.isDirectory) {
//            buildListOfFilesInDirectory(file)
//            history.add(file)
//        }
//    }

//    fun exitDirectory() {
//
//        if (history.isNotEmpty()) history.removeLast()
//        if (history.isNotEmpty()) buildListOfFilesInDirectory(history.removeLast())
//    }

//    private fun buildListOfFilesInDirectory(directory: File) {
//
////        val s = "${directory.absolutePath}/${directory.name}"
////
////        log(s.removePrefix(rootDirectory))
////        log(directory.canonicalPath)
////        log(directory.parent)
//        viewModelScope.launch(Dispatchers.IO) {
//            val list = directory.listFiles()
//
//            _filesInDirectoryList.apply {
//                clear()
//                addAll(list)
//                log("buildListOfFilesInDirectory")
//                _upd.postValue("")
////                    sortBy { it.name }
////                _uiState.update { DirectoryViewerUiState.DirectoryListUpdated(list.asList()) }
//            }
//
////            if (list != null)
////            withContext(Dispatchers.Main) {
////                _upd.value = ""
////            }
//
//        }



//        .let { list ->
//
//                _filesInDirectoryList.clear()
//                _filesInDirectoryList.addAll(list)
//                _filesInDirectoryList.sortBy { it.name }

//            for (file in list) {
//                val f = cashedFiles(get)
//                if (cashedFiles)
//                _filesInDirectoryList.add(file)

//                if (file.isDirectory) _filesInDirectoryList.add(DirectoryItem(path, i.name, true))
//                else _filesInDirectoryList.add(DirectoryItem(path, i.name, false))
//            }

//    }

//    fun uiStateConsumed() = _uiState.update { SignFileUiState.Initial }

