package jp.gr.aqua.layoutforkeyone

import android.content.Context
import android.content.Intent
import android.hardware.input.InputDeviceIdentifier
import android.hardware.input.InputManager
import android.view.InputDevice
import rx.Observable

class KeyboardEnumerator(val context : Context){

    data class KeyboardInfo(
            val deviceName: String,
            val identifier : InputDeviceIdentifier,
            val keyboardLayout : String
    )

    fun showKeyboardLayoutScreen(info : InputDeviceIdentifier) : Intent {
        return Intent(ACTION_INPUT_METHOD_SETTINGS).apply{
            setClassName("com.android.settings", "com.android.settings.Settings\$InputMethodAndLanguageSettingsActivity")
            putExtra(EXTRA_INPUT_DEVICE_IDENTIFIER, info )
        }
    }

    fun getKeyboards() : Observable<KeyboardInfo> {
        return Observable.create {
            subscriber->
            val im = context.getSystemService(InputManager::class.java)
            val devices = InputDevice.getDeviceIds()
            devices.forEach {
                val device = InputDevice.getDevice(it)
                if (device != null
                        && !device.isVirtual
                        && device.isFullKeyboard) {

                    val title = device.name
                    val identifier = device.identifier
                    val keyboardLayoutDescriptor = im.getCurrentKeyboardLayoutForInputDevice(identifier)
//                    Log.d("=====>","device=$title / identifier=$identifier / summary=$summary / layout=${keyboardLayoutDescriptor}")

                    val keyboardLayout = if (keyboardLayoutDescriptor != null)
                        im.getKeyboardLayout(keyboardLayoutDescriptor)
                    else
                        null
                    val summary = keyboardLayout?.toString()?:"Default"

                    subscriber.onNext(KeyboardInfo(title,identifier,summary))
                }
            }
            subscriber.onCompleted()
        }

    }

    companion object {
        val EXTRA_INPUT_DEVICE_IDENTIFIER = "input_device_identifier"
        val ACTION_INPUT_METHOD_SETTINGS = "android.settings.INPUT_METHOD_SETTINGS"
    }


}



