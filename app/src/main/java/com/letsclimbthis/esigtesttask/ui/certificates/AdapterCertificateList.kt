package com.letsclimbthis.esigtesttask.ui.certificates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.letsclimbthis.esigtesttask.databinding.CertificatesListItemBinding
import com.letsclimbthis.esigtesttask.ui.utils.getCN
import java.security.cert.X509Certificate

class AdapterCertificateList (
    private var certificatesList: List<X509Certificate>,
    private val outerClickHandler: OnCertificateListItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    // реализуется экземпляром FragmentCertificates для обработки клика
    interface OnCertificateListItemClickListener {
        fun onListItemClick(viewClicked: View, itemIndexInList: Int)
    }

    inner class CertificatesItemViewHolder(val binding: CertificatesListItemBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener
    {
        override fun onClick(p0: View?) {
            p0?.let { outerClickHandler.onListItemClick(p0, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CertificatesListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CertificatesItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder as CertificatesItemViewHolder) {
            with(binding) {
                tvCertificateSerial.text = certificatesList[position].serialNumber.toString()
                tvCertificateIssuerName.text = certificatesList[position].getCN()
                tvCertificateExpirationDate.text = certificatesList[position].notAfter.toString()
            }
        }
    }

    override fun getItemCount() = certificatesList.size

}


//        init {
//            setHasStableIds(true)
//        }

//        override fun getItemId(position: Int) = position.toLong()
//
//        override fun getItemViewType(position: Int) = position


//        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
//            super.onViewRecycled(holder)
//            binding.directoryItem = null
//        }


//        private lateinit var binding: CertificatesListItemBinding
