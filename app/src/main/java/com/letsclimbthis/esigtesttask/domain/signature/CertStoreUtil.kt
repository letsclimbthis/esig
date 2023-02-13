package com.letsclimbthis.esigtesttask.domain.signature

import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.utils.getIssuerName
import com.letsclimbthis.esigtesttask.ui.utils.getSubjectName
import ru.CryptoPro.JCSP.CSPConfig
import ru.CryptoPro.JCSP.support.BKSTrustStore
import ru.CryptoPro.JCSP.JCSP
import java.io.*
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CertStoreUtil {

//    // returns list of certificates or an empty list in case of error
//    fun loadRootCertStoreCertificates(): List<X509Certificate> {
//        log("$CLASS_NAME.loadTrustCertStoreCertificates() call")
//        return getKeyStore(getRootCertStorePath(), getRootCertStoreName())?.let { loadCertStoreCertificates(it) } ?: listOf()
//    }

//    // returns list of certificates or an empty list in case of error
//    fun loadCommonCertStoreCertificates(): List<X509Certificate> {
//        log("$CLASS_NAME.loadCommonCertStoreCertificates() call")
//        return getKeyStore(getCommonCertStorePath(), getCommonCertStoreName())?.let { loadCertStoreCertificates(it) } ?: listOf()
//    }

    // returns array of lists of certificates or an empty list in case of error
    // array[0] - list of root certificates
    // array[1] - list of common certificates
    fun loadCertificatesFromStoreByCategory(): Array<List<X509Certificate>> {
        log("$CLASS_NAME.loadCertificatesFromStoreByCategory() call")
        val list = getKeyStore(getRootCertStorePath(), getRootCertStoreName())?.let { loadCertStoreCertificates(it) } ?: listOf()
        val root = list.filter { it.getIssuerName() == it.getSubjectName()}
        val common = list.filter { it.getIssuerName() != it.getSubjectName()}
        log("$CLASS_NAME.loadCertificatesFromStoreByCategory(): certificate list size: root= ${root.size}, common = ${common.size} ")
        return arrayOf(root,common)
    }

//    fun saveCertificateToRootCertStore(certFile: File) {
//        log("$CLASS_NAME.saveCertificateToAppTrustStore() call")
//        val certificate = createCertFromFile(certFile)
//        certificate?.let {
//            saveCertificateToCertStore(
//                getRootCertStorePath(),
//                getRootCertStoreName(),
//                it
//            )
//        }
//    }

//    fun saveCertificateToCommonCertStore(certFile: File) {
//        log("$CLASS_NAME.saveCertificateToAppCommonStore() call")
//        val certificate = createCertFromFile(certFile)
//        certificate?.let {
//            saveCertificateToCertStore(
//                getCommonCertStorePath(),
//                getCommonCertStoreName(),
//                it
//            )
//        }
//    }

    fun saveCertificateToCertStore(certFile: File): X509Certificate? {
        var certificate: X509Certificate? = null
        log("$CLASS_NAME.saveCertificateToCertStore(): Start adding certificate in store")
        certificate = createCertFromFile(certFile)
        certificate?.let {
            saveCertificateToCertStore(
                getRootCertStorePath(),
                getRootCertStoreName(),
                it
            )
        } ?: log("$CLASS_NAME.saveCertificateToCertStore(): Can't add certificate in store")
        return certificate
    }

    fun deleteCertificate(cert: X509Certificate) {
        log("$CLASS_NAME.deleteCertificate() with alias '${cert.alias()}'")
        val ks = getKeyStore(getRootCertStorePath(), getRootCertStoreName())
        try {
            ks?.let {
                it.deleteEntry(cert.alias())
                updateCertStore(getRootCertStorePath(), it)
                log("$CLASS_NAME.deleteCertificate(): The certificate was deleted successfully")
            }
        } catch (e: Exception) {
            log("$CLASS_NAME.deleteCertificate(): ", e)
        }
    }

    fun isCertificateInStore(cert: X509Certificate): Boolean {
        val ks = getKeyStore(getRootCertStorePath(), getRootCertStoreName())
        val bool = ks?.getCertificate(cert.alias()) != null
        log("$CLASS_NAME.isCertificateInStore() = $bool")
        return bool
    }

    fun printRootCertStoreContent() {
        getKeyStore(getRootCertStorePath(), getRootCertStoreName())?.let { printCertStoreContent(it) }
    }

    fun printCommonCertStoreContent() {
        getKeyStore(getCommonCertStorePath(), getCommonCertStoreName())?.let { printCertStoreContent(it) }
    }

    private fun loadCertStoreCertificates(keyStore: KeyStore): List<X509Certificate> {
        val result = mutableListOf<X509Certificate>()
        try {
            keyStore.let {
                val aliases = it.aliases()
                if (aliases.hasMoreElements()) {
                    while (aliases.hasMoreElements()) {
                        result.add(it.getCertificate(aliases.nextElement()) as X509Certificate)
                    }
                } else {
                    log("$CLASS_NAME.loadCertStoreCertificates(): KeyStore.aliases() returned empty list")
                }
            }
        } catch (e: Exception) {
            log("$CLASS_NAME.loadCertStoreCertificates(): ", e)
        }
        log("$CLASS_NAME.loadCertStoreCertificates(): ${result.size} loaded certificates")
        return result
    }

    // converts given file to a X509Certificate instance
    // or throw an exception
    private fun createCertFromFile(certFile: File): X509Certificate? {
        log("$CLASS_NAME.createCertFromFile() for file ${certFile.name}")
        val stream = certFile.inputStream()
        return loadCert(stream)
    }

    private fun loadCert(certStream: InputStream): X509Certificate? {
        var result: X509Certificate? = null
        try {
            val factory = CertificateFactory.getInstance("X.509")
            result =  certStream.use { factory.generateCertificate(it) as X509Certificate }
            log("$CLASS_NAME.loadCert(): loaded cert ${result.alias()}")
        }  catch (e: Exception) {
            log("$CLASS_NAME.loadCert(): ", e)
        }
        return result
    }

    // requests a KeyStore instance and save provided certificate
    // only if it is not present int the store
    private fun saveCertificateToCertStore(certStorePath: String, certStoreName: String, cert: X509Certificate) {
        log("$CLASS_NAME.saveCertificateToCertStore(): for certificate with alias ${cert.alias()}")
        getKeyStore(certStorePath, certStoreName)?.let { ks ->
            val certIsNotPresentInStore = ks.getCertificateAlias(cert) == null
            try {
                if (certIsNotPresentInStore) {
                    ks.setCertificateEntry(cert.alias(), cert)
                    updateCertStore(certStorePath, ks)
                    log("$CLASS_NAME.saveCertificateToCertStore(): The certificate was added successfully.")
                } else {
                    log("$CLASS_NAME.saveCertificateToCertStore(): The certificate has already existed in the store")
                }
            } catch (e: Exception) {
                log("$CLASS_NAME.saveCertificateToCertStore(): ", e)
            }
        }
    }

    // returns KeyStore instance representing a particular certificate storage file in the device
    // or just 'empty' KeyStore instance if there is no any file
    private fun getKeyStore(certStorePath: String, certStoreName: String): KeyStore? {
        log("$CLASS_NAME.getKeyStore(): trying to obtain KeyStore for certStorePath='$certStorePath' and certStoreName='$certStoreName'}")
        var keyStore: KeyStore? = null

        try {
            // init keystore according to JCA docs
            // the type of keystore represented by 'certStoreName',
            // which is implementation of keystore of type 'JKS' from current CSP
            keyStore =
                if (isCertStoreExist(certStorePath)) {
                    // loading content of existing keyStore (from a file) into memory
                    KeyStore.getInstance(certStoreName).apply {
                        FileInputStream(certStorePath).use { fis ->
                            load(fis, getPassword())
                        }
                    }
                } else {
                    // creating new empty keyStore
                    KeyStore.getInstance(certStoreName).apply {
                        load(null, null)
                    }
                }
        } catch (e: Exception) {
            log("$CLASS_NAME.getKeyStore(): ", e)
        }
        return keyStore
    }

    private fun isCertStoreExist(certStorePath: String): Boolean {
        val certStoreFile = File(certStorePath)
        return with(certStoreFile) {
            if (this.exists()) {
                log("$CLASS_NAME.isCertStoreExist(): Certificate store '$certStorePath' exists")
                true}
            else {
                log("$CLASS_NAME.isCertStoreExist(): Certificate store '$certStorePath' doesn't exist")
                false
            }
        }
    }

    private fun updateCertStore(certStorePath: String, keystore: KeyStore) {
        try {
            val certStoreFile = File(certStorePath)
            FileOutputStream(certStoreFile).use {
                keystore.store(it, getPassword())
            }
            log("$CLASS_NAME.updateCertStore(): The certificate store was updated successfully.")
        } catch (e: Exception) {
            log("$CLASS_NAME.updateCertStore(): ", e)
        }
    }

    private fun X509Certificate.alias() = serialNumber.toString(16)

    // getTrustStorePath() = "/data/user/0/com.letsclimbthis.esigtesttask/security/cacerts"
    private fun getRootCertStorePath() = CSPConfig.getBksTrustStore() + File.separator + BKSTrustStore.STORAGE_FILE_TRUST
    private fun getRootCertStoreName() = BKSTrustStore.STORAGE_TYPE

    // getCommonStorePath() = "/data/user/0/com.letsclimbthis.esigtesttask/security/certstore"
    private fun getCommonCertStorePath() = CSPConfig.getBksTrustStore() + File.separator + "certstore"
    private fun getCommonCertStoreName() = JCSP.CERT_STORE_NAME

    // password is used to calculate an integrity checksum of the keystore data,
    // which is appended to the keystore data
    private fun getPassword() = BKSTrustStore.STORAGE_PASSWORD

    private fun printCertStoreContent(keyStore: KeyStore) {
        try {
            keyStore.let {
                val aliases = it.aliases()
                if (aliases != null) {
                    log("$CLASS_NAME.printCertStoreContent(): certificates in store type: ${keyStore.type}:")
                    while (aliases.hasMoreElements()) {
                        val certificate = it.getCertificate(aliases.nextElement()) as X509Certificate
                        log("${certificate.subjectDN}")
                    }
                }
            }
        } catch (e: Exception) {
            log("$CLASS_NAME.printCertStoreContent(): ", e)
        }
    }

    private const val CLASS_NAME = "CertStoreUtil"

}