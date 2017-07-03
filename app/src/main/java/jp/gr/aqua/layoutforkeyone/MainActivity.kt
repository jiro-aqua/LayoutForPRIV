package jp.gr.aqua.layoutforkeyone

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MainActivity : AppCompatActivity()
{
    private val TAG = "=====>"

    private val recyclerView by lazy {findViewById(R.id.recyclerView) as RecyclerView }
    private val helpButton by lazy {findViewById(R.id.help) as Button }
    private val ossButton by lazy {findViewById(R.id.oss) as Button }

    private val notificationSwitch by lazy {findViewById(R.id.notification_switch) as Switch }
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = LinearLayoutManager(this)

    }

    override fun onResume() {
        super.onResume()

        val keyboardEnumerator = KeyboardEnumerator(this)
        keyboardEnumerator.getKeyboards()
                .subscribeOn(Schedulers.io())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    infos->
                    recyclerView.adapter = RecyclerAdapter(this,infos){
                        info->
                        keyboardEnumerator.showKeyboardLayoutScreen(info).let{
                            startActivity( it )
                            val device = info.getDevice()
                            val ime = info.getIme(this)
                            sharedPreferences.edit()
                                    .putString(KEY_RESET_DEVICE,device)
                                    .putString(KEY_RESET_IME,ime)
                                    .apply()
                            if ( device == "stmpe_keypad" ){
                                Toast.makeText(this,R.string.toast_select_ime,Toast.LENGTH_LONG).show()
                            }else{
                                Toast.makeText(this,R.string.toast_select_keyboard,Toast.LENGTH_LONG).show()
                            }
                            startService(Intent(this, MonitorService::class.java))
                            finish()
                        }
                    }
                }


        val agree = {
            sharedPreferences.edit().putBoolean(KEY_AGREEMENT,true).apply()
            AlertDialog.Builder(this).setTitle(R.string.app_label)
                    .setMessage( getAssets().open("help.txt").reader(charset=Charsets.UTF_8).use{it.readText()} )
                    .setPositiveButton(R.string.label_ok,null)
                    .show()
        }

        val disagree = {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=jp.gr.aqua.layoutforkeyone"))
            startActivity(storeIntent)
            Toast.makeText(this,R.string.toast_uninstall,Toast.LENGTH_LONG).show()
            finish()
        }

        helpButton.setOnClickListener { agree() }

        ossButton.setOnClickListener {
            AlertDialog.Builder(this).setTitle(R.string.app_label)
                    .setMessage( getAssets().open("notice.txt").reader(charset=Charsets.UTF_8).use{it.readText()} )
                    .setPositiveButton(R.string.label_ok,null)
                    .show()
        }

        sharedPreferences.getString(KEY_RESET_DEVICE,null)?.let {
            startService(Intent(this, MonitorService::class.java))
        }

        sharedPreferences.getBoolean(KEY_AGREEMENT,false).then {
            AlertDialog.Builder(this).setTitle(R.string.app_label)
                    .setMessage( getAssets().open("caution.txt").reader(charset=Charsets.UTF_8).use{it.readText()} )
                    .setCancelable(false)
                    .setPositiveButton(R.string.label_agree,{di,i->agree()})
                    .setNegativeButton(R.string.label_disagree,{di,i->disagree()})
                    .show()
        }

    }

    private class RecyclerAdapter(private val context: Context,
                                  private val data: List<KeyboardEnumerator.KeyboardInfo>,
                                  private val listener: (KeyboardEnumerator.KeyboardInfo)->Unit )
        : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

        private val mInflater: LayoutInflater by lazy {LayoutInflater.from(context)}
        private val pm by lazy { context.packageManager }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerAdapter.ViewHolder {
            // 表示するレイアウトを設定
            return ViewHolder(mInflater.inflate(R.layout.keyboard_row, viewGroup, false))
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            if (data.size > i) {
                // データ表示
                setInfo(viewHolder, data[i])
                // クリック処理
                viewHolder.itemView.setOnClickListener { listener(data[i]) }
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        // ViewHolder(固有ならインナークラスでOK)
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val keyboard by lazy { itemView.findViewById(R.id.keyboard) as TextView }
            val imi: TextView by lazy { itemView.findViewById(R.id.imi) as TextView }
            val subtype: TextView by lazy { itemView.findViewById(R.id.subtype) as TextView }
            val layout: TextView by lazy { itemView.findViewById(R.id.layout) as TextView }
        }

        private fun setInfo( viewHolder:ViewHolder , info: KeyboardEnumerator.KeyboardInfo ) {
            viewHolder.keyboard.text = info.deviceInfo.mDeviceName

            val imi = info.imi
            val imSubtype = info.imSubtype

            viewHolder.imi.text = imi.loadLabel(pm)

            try {
                val appinfo = pm.getApplicationInfo(imi.packageName, 0)
                viewHolder.subtype.text = imSubtype!!.getDisplayName(context, imi.packageName, appinfo)
            } catch (e: PackageManager.NameNotFoundException) {
                viewHolder.subtype.text = ""
            }

            viewHolder.layout.text = info.layout.toString()
        }
    }

    companion object {
        val KEY_AGREEMENT = "AGREEMENT";
        val KEY_RESET_DEVICE = "DEVICE";
        val KEY_RESET_IME = "IME";
    }

    private fun Boolean.then( block : ()->Unit ) = { if ( this ) block() }
}