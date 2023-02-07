package com.letsclimbthis.esigtesttask.ui.signfile.uistate

import java.io.File

sealed class FileState : StateComponent {

    object Initial: FileState()

    object FileNotChosen: FileState()

    data class FileChosen(val file: File): FileState()
}
