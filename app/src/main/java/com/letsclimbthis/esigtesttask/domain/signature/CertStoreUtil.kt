package com.letsclimbthis.esigtesttask.domain.signature

import com.letsclimbthis.esigtesttask.log
import com.letsclimbthis.esigtesttask.ui.utils.getIssuerName
import com.letsclimbthis.esigtesttask.ui.utils.getSubjectName
import ru.CryptoPro.JCP.JCP
import ru.CryptoPro.JCSP.CSPConfig
import ru.CryptoPro.JCSP.support.BKSTrustStore
import ru.CryptoPro.JCSP.JCSP
import java.io.*
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CertStoreUtil {

    // returns list of certificates or an empty list in case of error
    fun loadRootCertStoreCertificates(): List<X509Certificate> {
        log("$CLASS_NAME.loadRootCertStoreCertificates() call")
        return getKeyStore(
            getRootCertStoreName()
        )?.let { loadCertStoreCertificates(it) } ?: listOf()
    }

    // returns list of certificates or an empty list in case of error
    fun loadCommonCertStoreCertificates(): List<X509Certificate> {
        log("$CLASS_NAME.loadCommonCertStoreCertificates() call")
        return getKeyStore(
            getCommonCertStoreName()
        )?.let { loadCertStoreCertificates(it) } ?: listOf()
    }

    // returns array of lists of certificates or an empty list in case of error
    // array[0] - list of root certificates
    // array[1] - list of common certificates
//    fun loadCertificatesFromStoreByCategory(): Array<List<X509Certificate>> {
//        log("$CLASS_NAME.loadCertificatesFromStoreByCategory() call")
//        val list = getKeyStore(getRootCertStorePath(), getRootCertStoreName())?.let { loadCertStoreCertificates(it) } ?: listOf()
//        val root = list.filter { it.getIssuerName() == it.getSubjectName()}
//        val common = list.filter { it.getIssuerName() != it.getSubjectName()}
//        log("$CLASS_NAME.loadCertificatesFromStoreByCategory(): certificate list size: root= ${root.size}, common = ${common.size} ")
//        return arrayOf(root,common)
//    }

    fun saveCertificateToRootCertStore(certFile: File): X509Certificate? {
        log("$CLASS_NAME.saveCertificateToAppTrustStore() call")
        val certificate = createCertFromFile(certFile)
        certificate?.let {
            saveCertificateToCertStore(getRootCertStoreName(), it)
        }
        return certificate
    }

    fun saveCertificateToCommonCertStore(certFile: File): X509Certificate? {
        log("$CLASS_NAME.saveCertificateToAppCommonStore() call")
        val certificate = createCertFromFile(certFile)
        certificate?.let {
            saveCertificateToCertStore(getCommonCertStoreName(), it)
        }
        return certificate
    }

//    fun saveCertificateToCertStore(certFile: File): X509Certificate? {
//        var certificate: X509Certificate? = null
//        log("$CLASS_NAME.saveCertificateToCertStore(): Start adding certificate in store")
//        certificate = createCertFromFile(certFile)
//        certificate?.let {
//            saveCertificateToCertStore(
//                getRootCertStorePath(),
//                getRootCertStoreName(),
//                it
//            )
//        } ?: log("$CLASS_NAME.saveCertificateToCertStore(): Can't add certificate in store")
//        return certificate
//    }

    fun deleteCertificateFromRootCertStore(cert: X509Certificate) {
        deleteCertificateFromCertStore(getRootCertStoreName(), cert)
    }

    fun deleteCertificateFromCommonCertStore(cert: X509Certificate) {
        deleteCertificateFromCertStore(getCommonCertStoreName(), cert)
    }

    fun isCertificateInRootCertStore(cert: X509Certificate): Boolean {
        return isCertificateInStore(getRootCertStoreName(), cert)
    }

    fun isCertificateInCommonCertStore(cert: X509Certificate): Boolean {
        return isCertificateInStore(getCommonCertStoreName(), cert)
    }

    private fun isCertificateInStore(certStoreName: String, cert: X509Certificate): Boolean {
        val ks = getKeyStore(certStoreName)
        val bool = ks?.getCertificate(cert.alias()) != null
        log("$CLASS_NAME: certificate with alias '${cert.alias()} is in store '$certStoreName' = $bool")
        return bool
    }

    fun printRootCertStoreContent() {
        getKeyStore(getRootCertStoreName())?.let { printCertStoreContent(it) }
    }

    fun printCommonCertStoreContent() {
        getKeyStore(getCommonCertStoreName())?.let { printCertStoreContent(it) }
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
    private fun saveCertificateToCertStore(certStoreName: String, cert: X509Certificate) {
        log("$CLASS_NAME.saveCertificateToCertStore(): for certificate with alias ${cert.alias()}")
        getKeyStore(certStoreName)?.let { ks ->
            val certIsNotPresentInStore = ks.getCertificateAlias(cert) == null
            try {
                if (certIsNotPresentInStore) {
                    ks.setCertificateEntry(cert.alias(), cert)
                    updateCertStore(resolveCertStorePath(certStoreName), ks)
                    log("$CLASS_NAME.saveCertificateToCertStore(): The certificate was added successfully.")
                } else {
                    log("$CLASS_NAME.saveCertificateToCertStore(): The certificate has already existed in the store")
                }
            } catch (e: Exception) {
                log("$CLASS_NAME.saveCertificateToCertStore(): ", e)
            }
        }
    }

    private fun deleteCertificateFromCertStore(certStoreName: String, cert: X509Certificate) {
        log("$CLASS_NAME.deleteCertificate() with alias '${cert.alias()} from store '$certStoreName'")
        val ks = getKeyStore(certStoreName)
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

    // returns KeyStore instance representing a particular certificate storage file in the device
    // or just 'empty' KeyStore instance if there is no any file
    private fun getKeyStore(certStoreName: String): KeyStore? {
        var keyStore: KeyStore? = null
        val certStorePath = resolveCertStorePath(certStoreName)
        val providerName = resolveProviderName(certStoreName)
        log("$CLASS_NAME.getKeyStore(): trying to obtain KeyStore for certStorePath='$certStorePath', certStoreName='$certStoreName', providerName='$providerName'}")

        try {
            // init keystore according to JCA docs
            // the type of keystore represented by 'certStoreName',
            // which is implementation of keystore of type 'JKS' from current CSP
            keyStore =
                if (isCertStoreExist(certStorePath)) {
                    // loading content of existing keyStore (from a file) into memory
                    if (providerName == getRootCertStoreProviderName()) {
                        KeyStore.getInstance(certStoreName).apply {
                            FileInputStream(certStorePath).use { fis ->
                                load(fis, getPassword())
                            }
                        }
                    } else {
                        KeyStore.getInstance(certStoreName, providerName).apply {
                            FileInputStream(certStorePath).use { fis ->
                                load(fis, getPassword())
                            }
                        }
                    }
                } else {
                    // creating new empty keyStore
                    KeyStore.getInstance(certStoreName, providerName).apply {
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

    // getRootCertStorePath() = "/data/user/0/com.letsclimbthis.esigtesttask/security/cacerts"
    private fun getRootCertStorePath() = CSPConfig.getBksTrustStore() + File.separator + BKSTrustStore.STORAGE_FILE_TRUST
    private fun getRootCertStoreName() = BKSTrustStore.STORAGE_TYPE
    private fun getRootCertStoreProviderName() = "BKS"

    // getCommonStorePath() = "/data/user/0/com.letsclimbthis.esigtesttask/security/certstore"
    private fun getCommonCertStorePath() = CSPConfig.getBksTrustStore() + File.separator + "certstore"
    private fun getCommonCertStoreName() = JCSP.CERT_STORE_NAME
    private fun getCommonCertStoreProviderName() = JCP.PROVIDER_NAME



    private fun resolveCertStorePath(certStoreName: String): String {
        return when(certStoreName) {
            getRootCertStoreName() -> getRootCertStorePath()
            getCommonCertStoreName() -> getCommonCertStorePath()
            else -> ""
        }
    }

    private fun resolveProviderName(certStoreName: String): String {
        return when(certStoreName) {
            getRootCertStoreName() -> getRootCertStoreProviderName()
            getCommonCertStoreName() -> getCommonCertStoreProviderName()
            else -> ""
        }
    }


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