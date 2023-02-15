package com.letsclimbthis.esigtesttask.ui.certificates.viewpager

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.letsclimbthis.esigtesttask.ui.certificates.ViewModelCertificates
import kotlin.properties.Delegates
import com.letsclimbthis.esigtesttask.databinding.FragmentCertificateListHolderBinding
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.certificates.CertificatesUiState
import com.letsclimbthis.esigtesttask.ui.certificates.recyclerview.AdapterCertificateList
import com.letsclimbthis.esigtesttask.ui.utils.URIPathHelper
import com.letsclimbthis.esigtesttask.ui.utils.displayMessage
import com.letsclimbthis.esigtesttask.ui.utils.toggleSection

private const val KEY_FOR_FRAGMENT_INDEX = "fragmentIndex"

class AdapterPager(fragment: Fragment, private val amountOfPages: Int) :
    FragmentStateAdapter(fragment)
{

    // this fragment class:
    // - represents a page of ViewPager2
    // - hosts a particular list of certificates deriving from view model
    class FragmentCertificateListHolder() :
        Fragment(),
        AdapterCertificateList.OnCertificateListItemClickListener,
        View.OnClickListener
    {

        private val viewModel: ViewModelCertificates by lazy {
            ViewModelProvider(requireActivity())[ViewModelCertificates::class.java]
        }

        private var _binding: FragmentCertificateListHolderBinding? = null
        private val binding get() = _binding!!

        // position of concrete FragmentCertificateListHolder instance in the page list of PagerAdapter
        private var positionInViewPagerAdapter by Delegates.notNull<Int>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentCertificateListHolderBinding.inflate(inflater, container, false)
            binding.clickHandler = this
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            resolveFragmentsPositionInViewPagerAdapter()
            subscribeUi()
            initRecyclerView()
        }

        // receiving position sent from 'PagerAdapter.createFragment()' method
        private fun resolveFragmentsPositionInViewPagerAdapter() {
            arguments?.takeIf { it.containsKey(KEY_FOR_FRAGMENT_INDEX) }?.apply {
                positionInViewPagerAdapter = getInt(KEY_FOR_FRAGMENT_INDEX)
            }
        }

        private fun initRecyclerView() {
            binding.recyclerViewCertificateList.let {

                it.layoutManager = LinearLayoutManager(requireContext())

                it.adapter = AdapterCertificateList(
                    listOf(),
                    this
                )

                it.addOnScrollListener(object: OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        binding.fabAddCertificate.let { fab ->
                            if (dy > 0)
                                fab.hide()
                            else if (dy < 0)
                                fab.show()
                        }
                    }
                })
            }
        }

        private var certificatesUiStateObserver: Observer<CertificatesUiState>? = null

        private fun subscribeUi() {
            certificatesUiStateObserver = Observer<CertificatesUiState> { state ->
                binding.recyclerViewCertificateList.adapter?.apply {
                    // processing state only if it has changed for current page
                    // 'state.pageIndex' points to the page where the state has changed
                    if (state.pageIndex == positionInViewPagerAdapter) {
                        when(state) {

                            is CertificatesUiState.CertificateListLoaded -> {
                                (this as AdapterCertificateList).setNewList(
                                    viewModel.getCertificateList(
                                        positionInViewPagerAdapter
                                    )
                                )
                            }

                            is CertificatesUiState.CertificateAdded -> {
                                notifyItemInserted(state.newIndex)
                            }

                            is CertificatesUiState.CertificateDeleted -> {
                                notifyItemRemoved(state.removedIndex)
                            }

                            is CertificatesUiState.Failed -> {
                                displayMessage(state.message)
                            }
                        }
                    }
                }
        }
        // starting listening for changes in ui state
        viewModel.uiState.observe(viewLifecycleOwner, certificatesUiStateObserver!!)
    }

        override fun onListItemClick(viewClicked: View, itemIndexInList: Int) {
            viewModel.deleteCertificate(positionInViewPagerAdapter, itemIndexInList)
        }

        override fun onClick(p0: View?) {
            log("$className.onClick(): ")
            p0?.let {
                when(p0.tag) {
                    "fab_certificate_list" -> {
                        log("fab_certificate_list")
                        getContent.launch("*/*")
                    }
                }
            }
        }

        // process uri of file picked with outer activity
        private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val filePath = URIPathHelper.getPath(requireContext(), uri)
                if(filePath != null) {
                    viewModel.addCertificate(positionInViewPagerAdapter, filePath)
                    log("$className: Received URI from outer activity for file: $filePath")

                } else {
                    log("$className: Received URI from outer activity is null")
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
            viewModel.uiState.removeObserver(certificatesUiStateObserver!!)
        }

        private val className = "FragmentCertificateListHolder"
    }

    override fun getItemCount() = amountOfPages

    override fun createFragment(position: Int): Fragment {
        val fragment = FragmentCertificateListHolder()
        // providing this fragment instance with it's position in adapter's list
        fragment.arguments = Bundle().apply {
            putInt(KEY_FOR_FRAGMENT_INDEX, position)
        }
        return fragment
    }
}