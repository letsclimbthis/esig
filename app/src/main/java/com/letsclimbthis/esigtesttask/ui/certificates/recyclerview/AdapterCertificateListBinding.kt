package com.letsclimbthis.esigtesttask.ui.certificates.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.letsclimbthis.esigtesttask.databinding.CertificatesListItemBinding
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.utils.getSubjectName
import com.letsclimbthis.esigtesttask.ui.utils.toDateDays
import com.letsclimbthis.esigtesttask.ui.utils.toDateMinutes
import com.letsclimbthis.esigtesttask.ui.utils.toggleSection
import java.security.cert.X509Certificate


// don't work properly with recyclerview - data isn't bind
// (in `onBindViewHolder()`) on first screen load
class AdapterCertificateListBinding (
    private var certificatesList: List<X509Certificate>,
    private val outerClickHandler: OnCertificateListItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    init {
        setHasStableIds(true)
    }
    // реализуется экземпляром FragmentCertificates для обработки клика
    interface OnCertificateListItemClickListener {
        fun onListItemClick(viewClicked: View, itemIndexInList: Int)
    }

    inner class CertificatesItemViewHolder(val binding: CertificatesListItemBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener
    {

        init {
            binding.clickHandler = this
        }

        override fun onClick(p0: View?) {
            log("onClick certificate list item")
            p0?.let {
                when(p0.tag) {

                    "ib_certificate_expand_collapse_properties" ->
                        binding.lytCertificateProperties.toggleSection(p0)

                    "ib_certificate_list_delete" -> {
                        outerClickHandler.onListItemClick(p0, adapterPosition)
                        log("onClick ib_certificate_list_delete")
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CertificatesListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CertificatesItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cert = certificatesList[position]
        with((holder as CertificatesItemViewHolder).binding) {
            // TODO: Add noInfo() extension
            tvCertificateSubject.text = cert.getSubjectName()
            tvCertificatePeriod.apply {
                val period = "${cert.notBefore.time.toDateDays()} - ${cert.notAfter.time.toDateDays()}"
                text = period
            }
            tvCertificateSerial.apply {
                val number = cert.serialNumber.toString()
                text = number
            }
            tvCertificateIssuerDetail.text = cert.issuerDN.toString()
            tvCertificateSubjectDetail.text = cert.subjectDN.toString()
            tvCertificatePeriodDetail.apply {
                val period = "not before: ${cert.notBefore.time.toDateMinutes()}" +
                        "\nnot after: ${cert.notAfter.time.toDateMinutes()}"
                text = period
            }
        }
    }

    override fun getItemCount() = certificatesList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setNewList() {

    }

}