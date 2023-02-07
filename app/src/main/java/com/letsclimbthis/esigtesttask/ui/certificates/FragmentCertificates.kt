package com.letsclimbthis.esigtesttask.ui.certificates

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.letsclimbthis.esigtesttask.R
import com.letsclimbthis.esigtesttask.databinding.FragmentCertificatesBinding
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.utils.URIPathHelper
import com.letsclimbthis.esigtesttask.ui.utils.displayMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentCertificates :
    Fragment(),
    OnClickListener,
    AdapterCertificateList.OnCertificateListItemClickListener
{

    private val viewModel: ViewModelCertificates by lazy {
        ViewModelProvider(this)[ViewModelCertificates::class.java]
    }

    private var _binding: FragmentCertificatesBinding? = null
    private val binding get() = _binding!!

    private var certificatesUiStateObserver: Observer<CertificatesUiState>? = null

    // tags of views that can process onClick()
    private lateinit var fab_certificate_list_tag: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertificatesBinding.inflate(inflater, container, false)
        binding.clickHandler = this
        return binding.root
    }

    override fun onListItemClick(viewClicked: View, itemIndexInList: Int) {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        fab_certificate_list_tag = resources.getString(R.string.fab_certificate_list_tag)

        subscribeUi()
        viewModel.loadCertificateList()
    }

    private fun initRecyclerView() {
        val onCertificateListItemClickListener = this

        binding.recyclerViewCertificateList.apply {
            adapter = AdapterCertificateList(
                viewModel.certificateList,
                onCertificateListItemClickListener
            ).apply {
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun subscribeUi() {
        certificatesUiStateObserver = Observer<CertificatesUiState> { state ->
            binding.recyclerViewCertificateList.adapter?.apply {
                when(state) {
                    is CertificatesUiState.CertificateListLoaded
                    -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            notifyDataSetChanged()
                        }
                    }
                    is CertificatesUiState.CertificateAdded
                    -> {
                        notifyItemInserted(state.newIndex)
                    }
                    is CertificatesUiState.CertificateDeleted
                    -> {
                        notifyItemRemoved(state.removedIndex)
                    }
                    is CertificatesUiState.Failed
                    -> {
                        displayMessage(state.message)
                    }
                }
            }
        }
        viewModel.uiState.observe(viewLifecycleOwner, certificatesUiStateObserver!!)
    }

    override fun onClick(p0: View?) {
        log("onClick")
        p0?.let {
            when(it.tag) {
                fab_certificate_list_tag ->
                {
                    log("onClick")

                    getContent.launch("*/*")
                }
                else -> {}
            }
        }
    }

    // process uri of file picked with outer activity
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val filePath = URIPathHelper.getPath(requireContext(), uri)

            if(filePath != null) {
                viewModel.addCertificate(filePath)
                log("FragmentCertificates: Received URI from outer activity for file: $filePath")

            } else {
                log("FragmentCertificates: Received URI from outer activity is null")

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        certificatesUiStateObserver?.let { viewModel.uiState.removeObserver(it) }
    }

}
