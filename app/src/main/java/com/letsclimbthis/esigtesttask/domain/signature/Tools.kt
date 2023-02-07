package com.letsclimbthis.esigtesttask.domain.signature

import ru.CryptoPro.JCP.JCP
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.cert.X509Certificate
import javax.crypto.IllegalBlockSizeException

 fun computeDigest(
        cryptoProviderName: String,
        digestAlgorithmName: String,
        filePath: String
    ): ByteArray {
        val file = File(filePath)
        val blocLength = 1024
        val fileLength = file.length()
        var buffer =
            if (fileLength >= blocLength) ByteArray(blocLength)
            else ByteArray(fileLength.toInt())
        val digester = MessageDigest.getInstance(digestAlgorithmName, cryptoProviderName)

        if (fileLength <= 0L) throw IllegalBlockSizeException("File cannot be empty while computing digest")

        try {
            file.inputStream().buffered().use { input ->
                if (fileLength <= blocLength) {
                    input.read(buffer)
                    digester.update(buffer)

                } else {
                    while (true) {
                        val size = input.read(buffer)
                        if (size <= 0) break
                        if (size < blocLength) buffer = buffer.copyOfRange(0, size - 1)
                        digester.update(buffer)
                    }
                }
            }

        } catch (e: IOException) {
            // TODO: log e
        }

        return digester.digest()
    }

    fun resolveJcpSignAlgorithmName(certificate: X509Certificate): String {
        val certAlg = certificate.publicKey.algorithm
        val ignoreCase = true

        return if (
            certAlg.equals(JCP.GOST_EL_2012_256_NAME, ignoreCase) ||
            certAlg.equals(JCP.GOST_DH_2012_256_NAME, ignoreCase)
        ) JCP.GOST_SIGN_2012_256_NAME
        else if (
            certAlg.equals(JCP.GOST_EL_2012_512_NAME, ignoreCase) ||
            certAlg.equals(JCP.GOST_DH_2012_512_NAME, ignoreCase)
        ) JCP.GOST_SIGN_2012_512_NAME
        else if (
            certAlg.contains("GOST")
        ) JCP.GOST_EL_SIGN_NAME
        else ""
    }

    fun resolveJcpDigestAlgorithmName(certificate: X509Certificate): String {
        val certAlg = certificate.publicKey.algorithm
        val ignoreCase = true

        return if (
            certAlg.equals(JCP.GOST_EL_2012_256_NAME, ignoreCase) ||
            certAlg.equals(JCP.GOST_DH_2012_256_NAME, ignoreCase)
        ) JCP.GOST_DIGEST_2012_256_NAME
        else if (
            certAlg.equals(JCP.GOST_EL_2012_512_NAME, ignoreCase) ||
            certAlg.equals(JCP.GOST_DH_2012_512_NAME, ignoreCase)
        ) JCP.GOST_DIGEST_2012_512_NAME
        else if (
            certAlg.contains("GOST")
        ) JCP.GOST_DIGEST_NAME
        else ""
    }


fun writeFile(file: File, byteData: ByteArray) {
    try {
        file.outputStream().buffered().use { it.write(byteData) }
    } catch(e: IOException) {
        // TODO: log e
    }
}

/*
    public static void writeFile(String filePath, byte[] byteData) throws IOException {

        File file = new File(filePath);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            fos.write(byteData);

        } finally {
            if (fos != null) { fos.close(); }
        }
    }
    */

    fun readFile(file: File): ByteArray {

        lateinit var result: ByteArray

        val fileLength = file.length().apply {
            if (this <= 0L) throw IllegalBlockSizeException("File cannot be empty")
        }

        val blocLength = 1024
        var buffer =
            if (fileLength >= blocLength) ByteArray(blocLength)
            else ByteArray(fileLength.toInt())

        try {
            result = file.inputStream().buffered().use { input ->

                if (fileLength <= blocLength) {
                    input.read(buffer)
                    buffer

                } else {
                    val intermediateResult = mutableListOf<Byte>()
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead <= 0) break
                        if (bytesRead < blocLength) buffer = buffer.copyOfRange(0, bytesRead - 1)
                        intermediateResult.addAll(buffer.toList())
                    }
                    intermediateResult.toByteArray()
                }
            }

        } catch (e: IOException) {
            // TODO: log e
        }

        return result
    }

/*
    public static byte[] readFile(File file) throws IOException {
        FileInputStream fis = null;
        byte[] result;

        try {
            fis = new FileInputStream(file);
            result = new byte[fis.available()];

            int bytesReadTotal = 0;
            int bytesReadLastIteration;

            do {
                bytesReadLastIteration = fis.read(result, bytesReadTotal, result.length - bytesReadTotal);
                bytesReadTotal += bytesReadLastIteration;

            } while(bytesReadLastIteration > 0);

        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        return result;
    }
* */
