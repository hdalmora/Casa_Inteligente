package com.example.pichau.casainteligente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast

import kotlinx.android.synthetic.main.app_bar_lampadas.*
import kotlinx.android.synthetic.main.content_lampadas.*

class LampadasActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    var led1On : String? = "Off"
    var led1PWM : String? = "0"

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))
        if (fromUser) {
            tv_lumin1.text = progress.toString()
            pref.savePreferenceString(Structure.instance.PREFS_PWM_LED_1, progress.toString())

            val data = progress.toString() + "\n"

            BluetoothApplication.instance.sendData(data.toByteArray())
            try {

            } catch (e: Exception) //IOException e)
            {
                e.printStackTrace()
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))

        when (buttonView!!.id) {
            switchLAMPADA1.id -> {
                if (isChecked) {
                    ligaLampada()
                    pref.savePreferenceString(Structure.instance.PREFS_LED_1_ON, "On")
                } else {
                    desligarLampada()
                    pref.savePreferenceString(Structure.instance.PREFS_LED_1_ON, "Off")
                }
            }
        }
    }

    private fun ligaLampada() {
        Toast.makeText(this, "Lâmpada ligada.", Toast.LENGTH_SHORT).show();
        BluetoothApplication.instance.sendData(Structure.instance.LAMPADA_ON_BT_STRING.toByteArray());
    }

    private fun desligarLampada() {
        Toast.makeText(this, "Lâmpada desligada.", Toast.LENGTH_SHORT).show();
        BluetoothApplication.instance.sendData(Structure.instance.LAMPADA_OFF_BT_STRING.toByteArray());
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lampadas)
        setSupportActionBar(toolbarLampadas)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);

        val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))

        switchLAMPADA1.setOnCheckedChangeListener(this)
        sk_luminosidade1.setOnSeekBarChangeListener(this)

        sk_luminosidade1.max = 10

        val voiceIntent : Intent = intent
        val voiceCommand = voiceIntent.getStringExtra("voiceControl");

        led1On = pref.getPreferenceString(Structure.instance.PREFS_LED_1_ON)

        if(voiceCommand != null) {
            Log.i("VoiceControl,Lampadas: ", voiceCommand)
            if (voiceCommand in Structure.instance.LAMPADA_ON_ARRAY) {
                //ligaLampada()
                led1On = "On"
                pref.savePreferenceString(Structure.instance.PREFS_LED_1_ON, "On")
            } else if (voiceCommand in Structure.instance.LAMPADA_OFF_ARRAY) {
                //desligarLampada()
                led1On = "Off"
                pref.savePreferenceString(Structure.instance.PREFS_LED_1_ON, "Off")
            } else {
                Log.i("VoiceControl,Lampadas: ", "Intensidade: " + voiceCommand);
                val intens : Int = voiceCommand.toInt()

                val intensReal = (intens * 10) / 100

                if (intensReal in 1..10) {
                    //Ligar a Lâmpada com a intensidade
                    pref.savePreferenceString(Structure.instance.PREFS_PWM_LED_1, intensReal.toString())
                    val data = intensReal.toString() + "\n"

                    BluetoothApplication.instance.sendData(data.toByteArray())
                }
            }
        }

        switchLAMPADA1.isChecked = led1On.equals("On")
        if(!switchLAMPADA1.isChecked) {
            desligarLampada()
        }

        if (!pref.getPreferenceString(Structure.instance.PREFS_PWM_LED_1).isEmpty()) {
            sk_luminosidade1.progress = pref.getPreferenceString(Structure.instance.PREFS_PWM_LED_1).toInt()
            tv_lumin1.text = pref.getPreferenceString(Structure.instance.PREFS_PWM_LED_1)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here

        if(item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun CompoundButton.setCustomChecked(value: Boolean, listener: CompoundButton.OnCheckedChangeListener) {
        setOnCheckedChangeListener(null)
        isChecked = value
        setOnCheckedChangeListener(listener)
    }

}
