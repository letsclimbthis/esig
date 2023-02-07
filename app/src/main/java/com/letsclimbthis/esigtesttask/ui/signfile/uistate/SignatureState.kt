package com.letsclimbthis.esigtesttask.ui.signfile.uistate

import java.security.cert.Certificate

sealed class SignatureState : StateComponent {

    object Initial: SignatureState()

    object SignatureNotBuilt: SignatureState()

    object SignatureBuilding: SignatureState()

    data class SignatureBuildingFailed(val exception: java.lang.Exception): SignatureState()


    // TODO: revise parameter
    data class SignatureBuilt(val certificate: Certificate): SignatureState()


}