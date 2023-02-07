package com.letsclimbthis.esigtesttask.domain.signature

import com.letsclimbthis.esigtesttask.log
import ru.CryptoPro.JCP.JCP
import ru.CryptoPro.JCSP.CSPConfig
import ru.CryptoPro.JCSP.support.BKSTrustStore
import java.io.*
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CertStoreUtil {

    fun loadTrustCertStoreCertificates(): List<X509Certificate> {
        return getKeyStore(getTrustStorePath(), getTrustStoreName())?.let { loadCertStoreCertificates(it) } ?: listOf()
    }

    fun loadCommonCertStoreCertificates(): List<X509Certificate> {
        return getKeyStore(getCommonStorePath(), getCommonStoreName())?.let { loadCertStoreCertificates(it) } ?: listOf()
    }

    fun saveCertificateToAppTrustStore(certFile: File): X509Certificate? {
        val certificate = createCertFromFile(certFile)
        certificate?.let {
            saveCertToStore(
                getTrustStorePath(),
                getTrustStoreName(),
                it
            )
        }
        return certificate
    }

    fun saveCertificateToAppCommonStore(certFile: File): X509Certificate? {
        val certificate = createCertFromFile(certFile)
        certificate?.let {
            saveCertToStore(
                getCommonStorePath(),
                getCommonStoreName(),
                it
            )
        }
        return certificate
    }

    fun printTrustCertStoreContent() {
        getKeyStore(getTrustStorePath(), getTrustStoreName())?.let { printCertStoreContent(it) }
    }

    fun printCommonCertStoreContent() {
        getKeyStore(getCommonStorePath(), getCommonStoreName())?.let { printCertStoreContent(it) }
    }

    private fun loadCertStoreCertificates(keyStore: KeyStore): List<X509Certificate> {
        val result = mutableListOf<X509Certificate>()
        try {
            keyStore.let {
                val aliases = it.aliases()
                if (aliases != null) {
                    while (aliases.hasMoreElements()) {
                        result.add(it.getCertificate(aliases.nextElement()) as X509Certificate)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun createCertFromFile(certFile: File): X509Certificate? {
        val stream = certFile.inputStream()
        return loadCert(stream)
    }

    private fun loadCert(certStream: InputStream): X509Certificate? {
        var result: X509Certificate? = null
        try {
            val factory = CertificateFactory.getInstance("X.509")
            result =  certStream.use { factory.generateCertificate(it) as X509Certificate }
        }  catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun saveCertToStore(certStorePath: String, certStoreName: String, cert: X509Certificate) {
        getKeyStore(certStorePath, certStoreName)?.let { ks ->
            val certAlias = cert.serialNumber.toString(16)
            val certIsNotPresentInStore = ks.getCertificateAlias(cert) == null
            try {
                if (certIsNotPresentInStore) {
                    val trustStoreFile = File(certStorePath)
                    ks.setCertificateEntry(certAlias, cert)
                    FileOutputStream(trustStoreFile).use {
                        ks.store(it, getPassword())
                    }
                    log("CertStoreUtil: The certificate was added successfully.")
                } else {
                    log("CertStoreUtil: The certificate has already existed in the trust store")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getKeyStore(certStorePath: String, certStoreName: String): KeyStore? {
        log("CertStoreUtil: trying to obtain KeyStore for certStorePath='$certStorePath' and certStoreName='$certStoreName'")
        var keyStore: KeyStore? = null
//        if (!isStoreIsExists(certStorePath)) return null
        try {
            // init keystore according to JCA docs
            keyStore = KeyStore.getInstance(certStoreName).apply {
                FileInputStream(certStorePath).use {
                    load(it, getPassword())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return keyStore
    }

    private fun isStoreIsExists(certificateStore: String): Boolean {
        val certStoreFile = File(certificateStore)
        return with(certStoreFile) {
            if (this.exists()) {
                log("CertStoreUtil: Certificate store '$certificateStore' exists")
                true}
            else {
                log("CertStoreUtil: Certificate store '$certificateStore' doesn't exist")
                false
            }
        }
    }

    // CSPConfig.getBksTrustStore() = appPath + File.separator + "security"
    // BKSTrustStore.STORAGE_FILE_TRUST = "cacerts"
    private fun getTrustStorePath() = CSPConfig.getBksTrustStore() + File.separator + BKSTrustStore.STORAGE_FILE_TRUST
    private fun getTrustStoreName() = BKSTrustStore.STORAGE_TYPE
    private fun getCommonStorePath() = "CertStore"
    private fun getCommonStoreName() = JCP.HD_STORE_NAME
    private fun getPassword() = BKSTrustStore.STORAGE_PASSWORD

    private fun printCertStoreContent(keyStore: KeyStore) {
        try {
            keyStore.let {
                val aliases = it.aliases()
                if (aliases != null) {
                    while (aliases.hasMoreElements()) {
                        val certificate = it.getCertificate(aliases.nextElement()) as X509Certificate
                        log(certificate.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}