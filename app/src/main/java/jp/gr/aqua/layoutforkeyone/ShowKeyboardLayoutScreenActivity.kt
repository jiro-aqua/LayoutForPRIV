package jp.gr.aqua.layoutforkeyone

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class ShowKeyboardLayoutScreenActivity : Activity()
{
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val device = sharedPreferences.getString(MainActivity.KEY_RESET_DEVICE, null)
        if (device != null ) {
            val keyboardEnumerator = KeyboardEnumerator(this)
            keyboardEnumerator.getKeyboards()
                    .subscribeOn(Schedulers.io())
                    .filter { it.deviceName == device }
                    .first()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        info ->
                        startActivity(KeyboardEnumerator(this).showKeyboardLayoutScreen(info.identifier))
                    }
        }
        finish()
    }
}