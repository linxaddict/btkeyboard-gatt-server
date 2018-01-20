package com.machineinsight_it.btkeyboard.ble

import java.util.*

object BtKeyboardServiceProfile {
    val controllerService: UUID = UUID.fromString("8e5473d9-3a31-4d7b-9ed1-229c95634318")

    val key1Characteristic: UUID = UUID.fromString("b24b81c5-a85c-44cd-b9c9-f22920e88f0b")
    val key1Descriptor: UUID = UUID.fromString("12a6e1ac-c5e1-4cd7-a202-31ff366d816c")

    val key2Characteristic: UUID = UUID.fromString("7e8d4bb5-1bc5-4903-82e6-42352dc55bfd")
    val key2Descriptor: UUID = UUID.fromString("05da9edf-ddcd-480b-a376-b4452e7944b1")

    val key3Characteristic: UUID = UUID.fromString("d59760c4-76d6-4f18-8412-06fc54875c22")
    val key3Descriptor: UUID = UUID.fromString("5f2ff221-4f15-4a56-ad0d-cad6ebecae77")

    val key4Characteristic: UUID = UUID.fromString("707f9318-6a4e-481f-8b69-952c9112b06f")
    val key4Descriptor: UUID = UUID.fromString("13a66e47-50e0-476e-802c-97ba8aeb4594")
}