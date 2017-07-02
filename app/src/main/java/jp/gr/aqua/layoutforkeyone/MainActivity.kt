package jp.gr.aqua.layoutforkeyone

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity()
{
    private val TAG = "=====>"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        KeyboardEnumerator(this).getKeyboards().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    printKeyboards(it)
                }
    }

    fun printKeyboards( info: KeyboardEnumerator.KeyboardInfo ) {

        val pm = packageManager

        Log.d(TAG, info.deviceInfo.mDeviceName)
        val imi = info.imi
        val imSubtype = info.imSubtype
        if (imi != null) {
            Log.d(TAG, "imi=" + imi.loadLabel(pm))

            try {
                val appinfo = pm.getApplicationInfo(imi.packageName, 0)
                Log.d(TAG, "subtype=" + imSubtype!!.getDisplayName(this, imi.packageName, appinfo))
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val imename = imi.loadLabel(pm).toString()
            Log.d(TAG, "layout=" + info.layout)
//                if (imename.contains("Google")) {
//                    showKeyboardLayoutScreen(
//                            keyboards.deviceInfo.mDeviceIdentifier, imi, imSubtype)
//                }
            //                    });
        }
    }

}