package com.machineinsight_it.btkeyboard.bluetooth.profile

import java.util.*

object BtKeyboardServiceProfile {
    val controllerService: UUID = UUID.fromString("8e5473d9-3a31-4d7b-9ed1-229c95634318")

    val key1Characteristic: UUID = UUID.fromString("b24b81c5-a85c-44cd-b9c9-f22920e88f0b")
    val key2Characteristic: UUID = UUID.fromString("7e8d4bb5-1bc5-4903-82e6-42352dc55bfd")
    val key3Characteristic: UUID = UUID.fromString("d59760c4-76d6-4f18-8412-06fc54875c22")
    val key4Characteristic: UUID = UUID.fromString("707f9318-6a4e-481f-8b69-952c9112b06f")

    val clientConfig: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val statePressed = byteArrayOf(0x01)
    val stateReleased = byteArrayOf(0x02)
}