package com.letsclimbthis.esigtesttask.domain.signature

import com.letsclimbthis.esigtesttask.log
import com.objsys.asn1j.runtime.*
import kotlinx.coroutines.*
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name
import ru.CryptoPro.JCP.params.OID
import ru.CryptoPro.JCP.tools.AlgorithmUtility
import ru.CryptoPro.JCP.tools.Array
import ru.CryptoPro.JCSP.JCSP
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.*
import java.util.*


object PKCS7 {

    suspend fun createSign(fileToSignPath: String, storeType: String, containerAlias: String) = coroutineScope {
        log("PKCS7.createSign2()")

        val keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME)
        keyStore.load(null, null)
        log("keyStore loaded")

        val signatureFileName = "$fileToSignPath.p7b"
        val certificate = keyStore.getCertificate(containerAlias)

        val inputBytes = Array.readFile(fileToSignPath)
        val privateKey = keyStore.getKey(containerAlias, null) as PrivateKey
        val algorithmOid = AlgorithmUtility.keyAlgToSignatureOid(privateKey.algorithm)
        val signature = Signature.getInstance(algorithmOid, JCSP.PROVIDER_NAME)

        // инициализация экземпляра подписи и создание значения подписи (ByteArray)
        // по хэшу от байтового значения файла по указанному пути
        signature.initSign(privateKey)
        signature.update(inputBytes)
        val signatureValue = signature.sign()

        log("signature value obtained")

        val result = buildSignature(
             signatureValue,
             certificate,
             keyStore.getCertificateChain(containerAlias)
         )

        Array.writeFile(signatureFileName, result )
    }

    /*
    За основу взят пример подписи из
    libs
    samples-2.0.41971-release-210521.jar
    CMS_samples
    CMS.java
    public static byte[] createCMSEx(byte[] var0, byte[] var1, Certificate var2, boolean var3) throws Exception
    */
    private fun buildSignature(
        signatureValue: ByteArray,
        clientCert: Certificate,
        keyStoreChain: kotlin.Array<Certificate>
    ) : ByteArray {

        // для проверки по CRL DP
        System.setProperty("com.sun.security.enableCRLDP", "true")
        // для загрузки сертификатов по AIA из сети
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true")
        System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true")

        // Идентификаторы алгоритмов хеширования и формирования подписи
        val digestOid = AlgorithmUtility.keyAlgToDigestOid(clientCert.publicKey.algorithm)
        val algorithmOid = AlgorithmUtility.keyAlgToKeyAlgorithmOid(clientCert.publicKey.algorithm)

        // Формируем контекст подписи формата PKCS7
        val contentInfo = ContentInfo()
        contentInfo.contentType = Asn1ObjectIdentifier(OID("1.2.840.113549.1.7.2").value)
        val signedData = SignedData()
        contentInfo.content = signedData
        signedData.apply {
            version = CMSVersion(1L)
            digestAlgorithms = DigestAlgorithmIdentifiers(1)
        }
        log("contentInfo initialized")


        // Добавляем идентификатор алгоритма хеширования
        val digestAlgorithmIdentifier = DigestAlgorithmIdentifier((OID(digestOid)).value)
        digestAlgorithmIdentifier.parameters = Asn1Null()
        signedData.digestAlgorithms.elements[0] = digestAlgorithmIdentifier

        // Подпись отсоединенная, поэтому содержимое (2й параметр encapContentInfo) отсутствует
        signedData.encapContentInfo = EncapsulatedContentInfo(Asn1ObjectIdentifier((OID("1.2.840.113549.1.7.1")).value), null as Asn1OctetString?)

        // Получаем цепочку сертификатов
        // пробуем получить цепочку сертификатов из объекта KeyStoreChain, который предоставляется
        // экземпляром KeyStore (абстракция ключевого хранилища, содержащего сертификат подписанта)
        val chainCertificates = if (keyStoreChain.size > 1) {
            log("keyStoreChain.size > 1")
            keyStoreChain.toList()
        }
        // или пробуем построить цепочку сертификатов самостоятельно
        else {
            log("keyStoreChain.size <= 1")

            // все корневые сертификаты необходимо добавить в TrustAnchor
            // для корректного построения цепочки
            val rootCerts = CertStoreUtil.loadTrustCertStoreCertificates()
            val trustAnchorSet = HashSet<TrustAnchor>()
            for (rc in rootCerts) {
                trustAnchorSet.add(TrustAnchor(rc, null))
            }
            log("trustAnchorSet initialized")


            val builderParams = PKIXBuilderParameters(trustAnchorSet, null as CertSelector?)
            builderParams.sigProvider = null as String?

            // все остальные сертификаты (промежуточные), по возможности, также необходимо добавить
            // в CertStore для того, чтобы алгоритм PKIX смог формировать из сертификатов цепочку
            // в правильном порядке сертификатов; при отсутствии необходимых для построения цепочки
            // сертификатов, будет произведена попытка загрузить недостающие сертификаты из сети,
            // при условии что в имеющихся сертификатах есть ссылки, на недостающие

            // получаем промежуточные сертификаты из локального хранилища
            val _certs = CertStoreUtil.loadCommonCertStoreCertificates().toTypedArray()

            val certs: MutableList<Certificate> = ArrayList(0)
            for (i in _certs.indices) certs.add(_certs[i])
            val collectionCertStoreParameters = CollectionCertStoreParameters(certs)
            val certStore = CertStore.getInstance("Collection", collectionCertStoreParameters)
            // обновляем builderParams
            builderParams.addCertStore(certStore)

            // добавляем сертификат подписанта в targetCert
            val selector = X509CertSelector()
            selector.certificate = clientCert as X509Certificate
            // обновляем builderParams
            builderParams.targetCertConstraints = selector
            builderParams.isRevocationEnabled = false

            // получаем certPath
            log("start initializing CertPathBuilder")
            val certPathBuilderResult = CertPathBuilder
                .getInstance("CPPKIX", "RevCheck")
                .build(builderParams) as PKIXCertPathBuilderResult
            log("CertPathBuilder initialized")
            val certPath = certPathBuilderResult.certPath
            log("certPath build")

            // validate certPath
            // val certPathValidator = CertPathValidator.getInstance("CPPKIX", "RevCheck")
            // certPathValidator.validate(certPath, builderParams)

            // построенная цепочка сертификатов
            certPath.certificates
        }

        // Готовим место размещения для сертификата(ов) в подписи
        val isNotNullAndNotEmpty = chainCertificates?.size?.let { it > 0 } ?: false

        if (isNotNullAndNotEmpty) {
            signedData.apply {
                certificates = CertificateSet(chainCertificates.size + 1)
                certificates.elements = Array(chainCertificates.size + 1) { CertificateChoices() }
            }
        } else {
            signedData.apply {
                certificates.elements = Array(1) { CertificateChoices() }
                certificates.elements[0] = CertificateChoices()
            }
        }

        // Помещаем сертификат подписанта в подпись
        val asnSignerCertificate = ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate()
        val asn1BerDecodeBuffer1 = Asn1BerDecodeBuffer(clientCert.encoded)
        asnSignerCertificate.decode(asn1BerDecodeBuffer1)
        signedData.apply {
            certificates.elements[0] = CertificateChoices()
            certificates.elements[0].set_certificate(asnSignerCertificate)
        }

        // Помещаем сертификаты из цепочки в подпись
        if (isNotNullAndNotEmpty) {
            for (i in chainCertificates.indices) {
                val cert = ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate()
                val decodeBuff = Asn1BerDecodeBuffer(chainCertificates[i].encoded)
                cert.decode(decodeBuff)
                signedData.apply {
                    certificates.elements[i + 1] = CertificateChoices()
                    certificates.elements[i + 1].set_certificate(cert)
                }
            }
        }

        // Добавляем информацию о подписанте
        signedData.apply {
            signerInfos = SignerInfos(1)
            signerInfos.elements[0] = SignerInfo()
            signerInfos.elements[0].version = CMSVersion(1L)
            signerInfos.elements[0].sid = SignerIdentifier()
        }
        val certificateIssuerEncoded = (clientCert as X509Certificate).issuerX500Principal.encoded
        val asn1BerDecodeBuffer2 = Asn1BerDecodeBuffer(certificateIssuerEncoded)
        val name = Name()
        name.decode(asn1BerDecodeBuffer2)
        val certificateSerialNumber = CertificateSerialNumber(clientCert.serialNumber)
        signedData.apply {
            signerInfos.elements[0].sid.set_issuerAndSerialNumber(
                IssuerAndSerialNumber(name, certificateSerialNumber)
            )
            signerInfos.elements[0].digestAlgorithm = DigestAlgorithmIdentifier((OID(digestOid)).value)
            signerInfos.elements[0].digestAlgorithm.parameters = Asn1Null()
            signerInfos.elements[0].signatureAlgorithm = SignatureAlgorithmIdentifier((OID(algorithmOid)).value)
            signerInfos.elements[0].signatureAlgorithm.parameters = Asn1Null()
            signerInfos.elements[0].signature = SignatureValue(signatureValue)
        }

        // Получаем закодированную подпись
        val asn1BerEncodeBuffer = Asn1BerEncodeBuffer()
        contentInfo.encode(asn1BerEncodeBuffer, true)
        return asn1BerEncodeBuffer.msgCopy
    }

}