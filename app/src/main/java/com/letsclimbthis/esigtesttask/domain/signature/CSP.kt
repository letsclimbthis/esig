package com.letsclimbthis.esigtesttask.domain.signature

import android.Manifest
import android.app.Activity
import com.letsclimbthis.esigtesttask.log
import ru.CryptoPro.JCP.JCP
import ru.CryptoPro.JCSP.CSPConfig
import ru.CryptoPro.JCSP.JCSP
import ru.CryptoPro.JCSP.support.BKSTrustStore
import ru.CryptoPro.reprov.RevCheck
import ru.cprocsp.ACSP.util.PermissionHelper
import java.io.*
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CSP {

    fun init(activity: Activity) {

        /*
       Инициализация выполняется один раз в главном потоке приложения,
       т.к. использует статические переменные.
       Метод init() выполняет предварительную инициализацию провайдера CSP в контексте приложения:
       создает иерархию папок cprocsp, копирует и проверяет лицензию, копирует конфигурацию,
       создается доверенное хранилище BKS в папке security приложения и т.п.
       В случае успеха метод вернет код CSPConfig.CSP_INIT_OK, в других случаях - код ошибки
       (при проблемах с копированием, созданием папок, нарушением целостности и т.п.).
       JInitCSP.aar – библиотека, предназначенная для инициализации провайдера CSP в android-приложении.
       */
        val initCode: Int = CSPConfig.init(activity)
        val initOk = initCode == CSPConfig.CSP_INIT_OK
        if(!initOk) throw java.lang.IllegalStateException()

        // Загрузка Java CSP (хеш, подпись, шифрование, генерация контейнеров).
        if (Security.getProvider(JCSP.PROVIDER_NAME) == null) {
            Security.addProvider(JCSP())
        }

        if (Security.getProvider(JCP.PROVIDER_NAME) == null) {
            Security.addProvider(JCP())
        }

        // Загрузка Revocation Provider (CRL, OCSP).
        if (Security.getProvider(RevCheck.PROVIDER_NAME) == null) {
            Security.addProvider(RevCheck())
        }

        // Запрос прав на запись.
        PermissionHelper.checkPermissions(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            PermissionHelper.PERMISSION_REQUEST_CODE_WRITE_STORAGE
        )
    }

    fun getCertAndAliasFromKeyContainers(storeType: String): List<Pair<String, X509Certificate>> {
        val keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME)
        keyStore.load(null, null)
        val result = mutableListOf<Pair<String, X509Certificate>>()
        for (alias in keyStore.aliases().toList()) {
            val cert = keyStore.getCertificate(alias)
            if (cert != null) {
                result.add(Pair(alias, cert as X509Certificate))
            }
        }
        return result
    }



}

// data/data/nameApp/security/cacerts
///data/data/com.letsclimbthis.esigtesttask/security/cacerts
// AndroidCAStore

