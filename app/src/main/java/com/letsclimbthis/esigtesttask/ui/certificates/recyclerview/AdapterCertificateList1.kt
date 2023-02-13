package com.letsclimbthis.esigtesttask.ui.certificates.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.letsclimbthis.esigtesttask.R
import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.utils.getSubjectName
import com.letsclimbthis.esigtesttask.ui.utils.toDateDays
import com.letsclimbthis.esigtesttask.ui.utils.toDateMinutes
import com.letsclimbthis.esigtesttask.ui.utils.toggleSection
import java.security.cert.X509Certificate

class AdapterCertificateList1 (
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

    inner class CertificatesItemViewHolder(val view: View) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener
    {
        val tvCertificateSubject: TextView = view.findViewById(R.id.tv_certificate_subject)
        val tvCertificatePeriod: TextView = view.findViewById(R.id.tv_certificate_period)
        val tvCertificateSerial: TextView = view.findViewById(R.id.tv_certificate_serial)
        val tvCertificateIssuerDetail: TextView = view.findViewById(R.id.tv_certificate_issuer_detail)
        val tvCertificateSubjectDetail: TextView = view.findViewById(R.id.tv_certificate_issuer_detail)
        val tvCertificatePeriodDetail: TextView = view.findViewById(R.id.tv_certificate_period_detail)
        private val lytCertificateProperties: ConstraintLayout = view.findViewById(R.id.lyt_certificate_properties)
        private val expand: ImageButton = view.findViewById(R.id.bt_certificate_expand_collapse_properties)
        private val delete: ImageButton = view.findViewById(R.id.ib_delete_certificate)

        init {
            expand.setOnClickListener(this)
            delete.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            log("onClick certificate list item")
            p0?.let {
                when(p0.tag) {

                    "ib_certificate_expand_collapse_properties" -> {
                        lytCertificateProperties.toggleSection(p0)
                        log("click")
                    }

                    "ib_certificate_list_delete" -> {
                        outerClickHandler.onListItemClick(p0, adapterPosition)
                        log("onClick ib_certificate_list_delete")
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.certificates_list_item, parent, false)
        return CertificatesItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cert = certificatesList[position]
        with((holder as CertificatesItemViewHolder)) {
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

    fun setNewList(list: List<X509Certificate>) {
        certificatesList = list
        notifyDataSetChanged()
    }

}