package com.letsclimbthis.esigtesttask.ui.signfile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.letsclimbthis.esigtesttask.R
import com.letsclimbthis.esigtesttask.databinding.FragmentSignFileBinding
import com.letsclimbthis.esigtesttask.domain.signature.CSP
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.signfile.uistate.FileState
import com.letsclimbthis.esigtesttask.ui.signfile.uistate.KeyContainerState
import com.letsclimbthis.esigtesttask.ui.signfile.uistate.SignFileUiState
import com.letsclimbthis.esigtesttask.ui.signfile.uistate.SignatureState
import com.letsclimbthis.esigtesttask.ui.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.CryptoPro.JCSP.JCSP
import java.io.File

class FragmentSignFile :
    Fragment(),
    View.OnClickListener
{
    private var _binding: FragmentSignFileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ViewModelSignFile by lazy {
        ViewModelProvider(this)[ViewModelSignFile::class.java]
    }
    private var currentUiState: SignFileUiState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUiComponents()
        subscribeUiComponents()
    }

    // list of all possible layouts in all sections that need to be collapsed
    private lateinit var contentLayoutList: MutableList<View>

    // list of key containers from usb key storage cashed for efficiency
    private lateinit var keyContainerAliasList: List<String>

    // current active layout in each section to be expanded
    // when section number or section label is clicked
    private lateinit var currentActiveLytAtChooseSection: ConstraintLayout
    private lateinit var currentActiveLytAtSignSection: ConstraintLayout
    private lateinit var currentActiveLytAtManageSection: ConstraintLayout

    // view tags for section number and section label
    // for resolving clicked view in onClick()
    private lateinit var tv_label_choose_file: String
    private lateinit var tv_label_sign_file: String
    private lateinit var tv_label_manage_signed_file: String

    // tags of views that can process onClick()
    private lateinit var v_choose_file_tag: String
    private lateinit var bt_cancel_choice_of_file_tag: String
    private lateinit var bt_confirm_choice_of_file_tag: String
    private lateinit var bt_load_key_containers_file_tag: String
    private lateinit var tv_chosen_key_container_name_tag: String
    private lateinit var bt_sign_file_tag: String
    private lateinit var bt_show_signed_file_in_folder_tag: String
//    private lateinit var bt_send_signed_file_tag: String

    private fun initUiComponents() {

        binding.clickHandler = this

        contentLayoutList = mutableListOf(
            binding.lytAddFile,
            binding.lytShowFile,
            binding.lytLoadKeyContainers,
            binding.lytShowKeyContainer,
            binding.lytManageSignedFile,
        )

        tv_label_choose_file = resources.getString(R.string.tv_label_choose_file_tag)
        tv_label_sign_file = resources.getString(R.string.tv_label_sign_file_tag)
        tv_label_manage_signed_file = resources.getString(R.string.tv_label_manage_signed_file_tag)

        v_choose_file_tag = resources.getString(R.string.v_choose_file_tag)
        bt_cancel_choice_of_file_tag = resources.getString(R.string.bt_cancel_choice_of_file_tag)
        bt_confirm_choice_of_file_tag = resources.getString(R.string.bt_confirm_choice_of_file_tag)
        bt_load_key_containers_file_tag = resources.getString(R.string.bt_load_key_containers_file_tag)
        tv_chosen_key_container_name_tag = resources.getString(R.string.tv_chosen_key_container_name_tag)
        bt_sign_file_tag = resources.getString(R.string.bt_sign_file_tag)
        bt_show_signed_file_in_folder_tag = resources.getString(R.string.bt_show_signed_file_in_folder_tag)
//        bt_send_signed_file_tag = resources.getString(R.string.bt_send_signed_file_tag)

        currentActiveLytAtChooseSection = binding.lytAddFile
        currentActiveLytAtSignSection = binding.lytLoadKeyContainers
        currentActiveLytAtManageSection = binding.lytManageSignedFile

    }

    private fun subscribeUiComponents() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    // update views when activity is launched or after onStop() call
                    if (currentUiState == null) {
                        updateViewsWithFileState(it.fileState)
                        updateViewsWithKeyContainerState(it.keyContainerState)
                        updateViewsWithSignatureState(it.signatureState)
                    }
                    else {
                        // update views only if the state has changed
                        if (it.fileState != currentUiState!!.fileState)
                            updateViewsWithFileState(it.fileState)
                        if (it.keyContainerState != currentUiState!!.keyContainerState)
                            updateViewsWithKeyContainerState(it.keyContainerState)
                        if (it.signatureState != currentUiState!!.signatureState)
                            updateViewsWithSignatureState(it.signatureState)
                    }

                    if (currentUiState != null)
                        it.message.let { msg ->
                            if (msg != currentUiState!!.message && msg.isNotEmpty())
                                displayMessage(msg)
                        }

                    // update current state
                    currentUiState = it
                }
            }
        }
    }

    private fun updateViewsWithFileState(newState: FileState) {
        when (newState) {
            is FileState.Initial
            -> {
                binding.lytShowFile.collapse()
            }
            is FileState.FileNotChosen
            -> {
                if(currentActiveLytAtChooseSection != binding.lytAddFile) {
                    currentActiveLytAtChooseSection.collapse()
                    currentActiveLytAtChooseSection = binding.lytAddFile
                    currentActiveLytAtChooseSection.expand()
                }
            }
            is FileState.FileChosen
            -> {
                if(currentActiveLytAtChooseSection != binding.lytShowFile) {
                    currentActiveLytAtChooseSection.collapse()
                    binding.apply {
                        val fileName = newState.file.name
                        tvChosenFileName.text = fileName
                        tvChosenFilePath.text = newState.file.path
                        tvChosenFileSize.text = newState.file.getSize()
                        tvChosenFileDate.text = newState.file.lastModified().toDateMinutes()
                        currentActiveLytAtChooseSection = lytShowFile
                    }
                    currentActiveLytAtChooseSection.expand()
                }
            }
        }
    }

    private fun updateViewsWithKeyContainerState(newState: KeyContainerState) {
        when (newState) {
            is KeyContainerState.Initial -> {
                binding.lytLoadKeyContainers.collapse()
                binding.progressLoadKeyContainers.visibility = View.GONE
            }
            is KeyContainerState.KeyContainersNotLoaded
            -> {
                if (currentActiveLytAtSignSection != binding.lytLoadKeyContainers) {
                    currentActiveLytAtSignSection.collapse()
                    currentActiveLytAtSignSection = binding.lytLoadKeyContainers
                    currentActiveLytAtSignSection.expand()
                }
            }
            is KeyContainerState.KeyContainersLoading
            -> {
                binding.btLoadKeyContainers.visibility = View.GONE
                binding.progressLoadKeyContainers.visibility = View.VISIBLE
            }
            is KeyContainerState.KeyContainersLoadingFailed
            -> {
                binding.progressLoadKeyContainers.visibility = View.GONE
                binding.btLoadKeyContainers.visibility = View.VISIBLE

            }
            is KeyContainerState.KeyContainersLoaded
            -> {
                binding.progressLoadKeyContainers.visibility = View.GONE
                showMenu(binding.tvChosenKeyContainerName, newState.containerList)
                keyContainerAliasList = newState.containerList
                }
            is KeyContainerState.KeyContainerChosen
            -> {
                if (currentActiveLytAtSignSection != binding.lytShowKeyContainer) {
                    currentActiveLytAtSignSection.collapse()
                    currentActiveLytAtSignSection = binding.lytShowKeyContainer
                    val cert = newState.container.second
                    binding.apply {
                        tvChosenKeyContainerName.text = newState.container.first
                        tvChosenKeyContainerOwnerName.text = cert.getSubjectName()
                        tvChosenKeyContainerCertificateNumber.apply {
                            val number = cert.serialNumber.toString()
                            text = number
                        }
                        tvChosenKeyContainerExpirationDate.apply {
                            val period = "${cert.notBefore.time.toDateDays()} - ${cert.notAfter.time.toDateDays()}"
                            text = period
                        }
                    }
                    currentActiveLytAtSignSection.expand()
                }
            }
        }
    }

    private fun updateViewsWithSignatureState(newState: SignatureState) {
        when (newState) {
            is SignatureState.Initial
            -> {
                binding.apply {
                    lytShowKeyContainer.collapse()
                    lytManageSignedFile.collapse()
                    progressSignFile.visibility = View.GONE
                    btShowSignedFileInFolder.isEnabled = false
//                    btSendSignedFile.isEnabled = false
                }
            }
            is SignatureState.SignatureNotBuilt
            -> {
            }
            is SignatureState.SignatureBuilding
            -> {
                binding.progressSignFile.visibility = View.VISIBLE
            }
            is SignatureState.SignatureBuilt
            -> {
                currentActiveLytAtSignSection.collapse()
                currentActiveLytAtManageSection.expand()
                binding.apply {
                    progressSignFile.visibility = View.GONE
                    btShowSignedFileInFolder.isEnabled = true
//                    btSendSignedFile.isEnabled = true
                }
            }
            is SignatureState.SignatureBuildingFailed
            -> {
                binding.progressSignFile.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {

        when(p0?.tag.toString()) {

            // section number or section label
            tv_label_choose_file
            -> {
                if(!currentActiveLytAtChooseSection.isVisible) {
                    currentActiveLytAtSignSection.collapse()
                    currentActiveLytAtManageSection.collapse()
                    currentActiveLytAtChooseSection.expand()
                }
            }
            tv_label_sign_file
            -> {
                if(!currentActiveLytAtSignSection.isVisible) {
                    currentActiveLytAtChooseSection.collapse()
                    currentActiveLytAtManageSection.collapse()
                    currentActiveLytAtSignSection.expand()
                }
            }
            tv_label_manage_signed_file
            -> {
                if(!currentActiveLytAtManageSection.isVisible) {
                    currentActiveLytAtChooseSection.collapse()
                    currentActiveLytAtSignSection.collapse()
                    currentActiveLytAtManageSection.expand()
                }
            }

            v_choose_file_tag
            -> {
                getContent.launch("*/*")
            }

            bt_cancel_choice_of_file_tag
            -> {
                viewModel.cancelChoiceOfFile()
            }

            bt_confirm_choice_of_file_tag
            -> {
                currentActiveLytAtChooseSection.collapse()
                currentActiveLytAtSignSection.expand()
            }

            bt_load_key_containers_file_tag
            -> {
//                for (s in CSP.getSupportedKeyContainers()) log(s)

                viewModel.loadKeyContainers()
            }

            tv_chosen_key_container_name_tag
            -> {
                p0?.let {showMenu(p0, keyContainerAliasList)}
            }

            bt_sign_file_tag
            -> {
                viewModel.signFile()
            }

            bt_show_signed_file_in_folder_tag
            -> {
                openFolder()
            }

//            bt_send_signed_file_tag
//            -> {
//                share()
//            }
        }
    }

    // process uri of file picked with outer activity
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val filePath = URIPathHelper.getPath(requireContext(), uri)

            if(filePath != null) {
                val file = File(filePath)
                viewModel.setChosenFileToSign(file)
                log("$className.getContent.launch(): Received URI from outer activity for file: ${file.path}")

            } else {
                log("$className.getContent.launch(): Received URI from outer activity is null")

            }
        }
    }

    // build menu consisting of key container aliases
    // and set key container as chosen when clicked menu item
    private fun showMenu(view: View, list: List<String>) {
        val popupMenu = PopupMenu(requireContext(), view)
        for (i in list.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, list[i])
        }
        popupMenu.setOnMenuItemClickListener {
            viewModel.setKeyContainer(it.itemId)
            false
        }
        popupMenu.show()
    }

    private fun openFolder() {
        val fileState = viewModel.uiState.value.fileState
        if (fileState is FileState.FileChosen) {
            val selectedUri = Uri.parse(fileState.file.path)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(selectedUri, "resource/folder")
            startActivity(intent)
        }
    }

    private fun share() {
        // TODO: implement
    }

    override fun onStop() {
        super.onStop()
        currentUiState = null
    }

    private val className = "FragmentSignFile"

}