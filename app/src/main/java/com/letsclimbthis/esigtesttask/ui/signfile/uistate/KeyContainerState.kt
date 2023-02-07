package com.letsclimbthis.esigtesttask.ui.signfile.uistate

import java.security.cert.Certificate
import java.security.cert.X509Certificate

sealed class KeyContainerState : StateComponent {

    object Initial : KeyContainerState()

    object KeyContainersNotLoaded : KeyContainerState()

    object KeyContainersLoading : KeyContainerState()

    object KeyContainersLoadingFailed : KeyContainerState()

//    data class KeyContainersLoaded(val containerList: List<String>) : KeyContainerState()
    data class KeyContainersLoaded(val containerList: List<String>) : KeyContainerState()

    data class KeyContainerChosen(val container: Pair<String, X509Certificate>) : KeyContainerState()
}
