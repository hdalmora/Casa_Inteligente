package com.example.pichau.casainteligente


import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.os.DeadObjectException
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.content.DialogInterface
import android.os.Build
import android.speech.RecognizerIntent
import android.support.annotation.RequiresApi
import java.sql.Struct
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, BtMessageReceivedListener {

    var alarmOn : Boolean = false

    val btApplication = BluetoothApplication.instance

    //var btApplication : BluetoothApplication? = BluetoothApplication()

    override fun stateProgress(state: Int) {

        if (state == Structure.instance.STATE_THREAD_BEGIN) {
            runOnUiThread {
                progressbar.visibility = View.VISIBLE
                btParearDisp.isEnabled = false
                btSelecionarDisp.isEnabled = false
                btSelecionarDisp.visibility = View.GONE
                btParearDisp.visibility = View.GONE
            }
        } else {
            runOnUiThread {
                progressbar.visibility = View.GONE
                btParearDisp.isEnabled = true
                btSelecionarDisp.isEnabled = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun receiveMessage(msg: String) {
        //Log.i("ReceivedMessage Main:", "Message: $msg")

        if (msg == "---N") {
            Log.i("messageHandler", "Ocorreu um erro durante a conexao")
            runOnUiThread {
                //stuff that updates ui

                btSelecionarDisp.visibility = View.GONE
                btParearDisp.visibility = View.VISIBLE

                tvAvisos.text = "Aviso !"
                tvAvisos2.text = "Conecte-se com o dispositivo pareado..."
                cvAlerta.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardViewAlert))
            }
            //btSelecionarDisp.visibility = View.VISIBLE
            message("Ocorreu um erro durante a conexão. Tente novamente, por favor...")
        } else if (msg == "---S") {
            Log.i("messageHandler", "Conectado com sucesso")
            //btSelecionarDisp.visibility = View.GONE
            runOnUiThread {
                //stuff that updates ui
                btSelecionarDisp.visibility = View.GONE
                btParearDisp.visibility = View.GONE
                progressbar.visibility = View.GONE

                tvAvisos.text = "Conectado com sucesso !"
                tvAvisos2.text = "O aplicativo esta pronto para uso"
                cvAlerta.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardViewGreenLightFlat))
            }
            //Toast.makeText(applicationContext, "Conectado com sucesso...", Toast.LENGTH_SHORT).show()
        } else {
            // Mensagem Recebida pela conexão bluetooth

            Log.i("messageHandler", "dataString: " + msg + " - Length: " + msg.length);

            if (msg == "ALARME") {
                //Setar Alarme
                Log.i("ALARME", "Alarme Ligado")
                Log.i("messageHandler", "dataString: $msg")

                alarmOn = true;

//                val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))
//                pref.savePreferenceString(Structure.instance.PREFS_ALARM, alarmOn!!)
                runOnUiThread {
                    cvAlarme.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCardViewAlert))
                    cvAlarme.isClickable = true
                    iconAlarme.setColorFilter(ContextCompat.getColor(this,  R.color.colorCardViewRED))
                    tvAlarme.text = "ALARME ACIONADO !"

                }
            } else {

                if(msg.get(0).toString() == "T") {
                    //Setar Temperatura
                    //TODO: Atualizar Views da Temperatura
                    runOnUiThread {
                        val temp = msg.substring(1)
                        Log.i("temperatura: ", temp)
                        tvTemperatura.text = temp
                    }
                } else {
                    //TODO: É mensagem para aparecer na textView de informações
                    runOnUiThread {
                        if(msg.get(0).toString() == "@") {
                            tvInformacoes.text = msg.substring(1);
                        }
                    }
                }

            }
        }
    }


    var mac_address : String? = ""

    fun message (msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    fun message (msg : String, duracao : Int) {
        Toast.makeText(this, msg, duracao).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == Structure.instance.ATIVAR_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i("Bluetooth", "Bluetooth ativado!");
                //TODO: Setar as Views para Bt Ativado
            } else {
                Log.i("Bluetooth", "Bluetooth não ativado!");
            }
        } else if(requestCode == Structure.instance.SELECIONAR_DISP_PAREADOS) {
            if (resultCode == Activity.RESULT_OK) {
                if(data!!.getStringExtra("btDevName").isEmpty() || data.getStringExtra("btDevAddress").isEmpty()) {
                    Log.i("Bluetooth", "Selecione um dispositivo pareado...");

                    runOnUiThread {
                        //stuff that updates ui
                        btSelecionarDisp.visibility = View.VISIBLE
                        btParearDisp.visibility = View.GONE

                        tvAvisos.text = "Aviso !"
                        tvAvisos2.text = "Um dispositivo deve ser selecionado..."
                        cvAlerta.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardViewAlert))
                    }
                } else {
                    Log.i("Bluetooth", "Voce selecionou: " + data.getStringExtra("btDevName") + "\n" + data.getStringExtra("btDevAddress"));

                    //btSelecionarDisp.visibility = View.GONE
                    connectToBluetoothAdapter(data.getStringExtra("btDevAddress"))
                    val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))
                    pref.savePreferenceString(Structure.instance.PREFS_MAC_ADDRESS, data.getStringExtra("btDevAddress"))
                    this.mac_address = data.getStringExtra("btDevAddress")
                    //btSelecionarDisp.visibility = View.GONE
                }
            }
        } else if (requestCode == Structure.instance.REQ_BLUETOOTH_ENABLE) {
            if (resultCode == 0) {
                Toast.makeText(applicationContext, "Bluetooth não ativado, encerrando a aplicação", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(applicationContext, "Bluetooth ativado", Toast.LENGTH_LONG).show();
                connectToBluetoothAdapter(mac_address!!)
            }
        } else if (requestCode == Structure.instance.VOICE_CONTORL_CODE) {
            if(resultCode == RESULT_OK && data != null) {
                val result : ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                //tvVoiceReturnText.text = result[0]
                message(result[0]);

                val command = result[0].toLowerCase();

                val voiceIntent = Intent(this, LampadasActivity::class.java)
                if (command in Structure.instance.LAMPADA_ON_ARRAY) {
                    voiceIntent.putExtra("voiceControl", command)
                    startActivity(voiceIntent)
                } else if (command.toLowerCase() in Structure.instance.LAMPADA_OFF_ARRAY) {
                    voiceIntent.putExtra("voiceControl", command)
                    startActivity(voiceIntent)
                }else if(command.toLowerCase() == "desligar alarme") {
                    //TODO: Checar se o alarme esta ligado, caso sim, desliga-lo
                    if(alarmOn) {
                        runOnUiThread {
                            cvAlarme.setBackgroundColor(ContextCompat.getColor(this, R.color.ColorDarkBlue))
                            cvAlarme.isClickable = false
                            iconAlarme.setColorFilter(ContextCompat.getColor(this, R.color.colorTextPrimary))
                            tvAlarme.text = "ALARME ON"
                        }

                        btApplication.sendData(Structure.instance.ALARM_OFF_BT_STRING.toByteArray())
                        alarmOn = false
                    } else {
                        message("O alarme não está acionado.")
                    }

                } else {
                    when {
                        command.contains("intensidade de", true) -> {
                            Log.i("voiceControl: ", "Comando: Intensidade DE")
                            val intensidade = command.substringAfter("intensidade de").substringAfter(" ").substringBefore("%");
                            Log.i("voiceControl", "Intens: " + intensidade);
                            try {
                                if(intensidade.toInt() in 1..100) {
                                    voiceIntent.putExtra("voiceControl", intensidade)
                                    startActivity(voiceIntent)
                                } else {
                                    message("Intensidade do LED inválida")
                                }
                            } catch (e : Exception) {
                                e.printStackTrace();
                                message("Comando inválido")
                            }

                        }
                        command.contains("intensidade", true) -> {
                            Log.i("voiceControl: ", "Comando: Intensidade")
                            try {
                                val intensidade = command.substringAfter("intensidade").substringAfter(" ").substringBefore("%");
                                Log.i("voiceControl", "Intens: " + intensidade);
                                if(intensidade.toInt() in 1..100) {
                                    voiceIntent.putExtra("voiceControl", intensidade)
                                    startActivity(voiceIntent)
                                } else {
                                    message("Intensidade do LED inválida")
                                }
                            } catch (e : Exception) {
                                e.printStackTrace()
                                message("Comando inválido - Erro ao executar")
                            }

                        }
                        else -> message("Comando não encontrado !", Toast.LENGTH_SHORT)
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        init()



        if (isBtAdapterEnabled()) {
            //btApplication = BluetoothApplication()

            val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))
            mac_address = pref.getPreferenceString(Structure.instance.PREFS_MAC_ADDRESS)

            //alarmOn = pref.getPreferenceString(Structure.instance.PREFS_ALARM)

            if (!mac_address!!.isEmpty()) {
                if (!btApplication.isBluetoothConnected()) {
                    message("Conectando...")
                    connectToBluetoothAdapter(mac_address!!)
                    Log.i("CONN", "MAC_ADDRESS onCreate: $mac_address")
//                    if (alarmOn!! == "ON") {
//                        cvAlarme.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCardViewAlert))
//                        cvAlarme.isClickable = true
//                        iconAlarme.setColorFilter(ContextCompat.getColor(this, R.color.colorCardViewRED))
//                        tvAlarme.text = "ALARME ACIONADO !"
//                    }
                }
//                } else {
//                    btSelecionarDisp.visibility = View.GONE
//                    btParearDisp.visibility = View.GONE
//                    progressbar.visibility = View.GONE
//
//                    tvAvisos.text = "Conectado com sucesso !"
//                    tvAvisos2.text = "O aplicativo esta pronto para uso"
//                    cvAlerta.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardViewGreenLightFlat))
//                }
            } else {
                Log.i("CONN", "MAC_ADDRESS onCreate: EMPTY")
                message("Nenhum dispositivo selecionado...")
            }

        } else {
            bluetoothRequestActivate();
        }
    }

    fun init () {
        btSelecionarDisp.setOnClickListener(this)
        btParearDisp.setOnClickListener(this)
        cvLampadas.setOnClickListener(this)
        cvAlarme.setOnClickListener(this)
        fabVoiceControl.setOnClickListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                message("Por favor, selecione um novo dispositivo", Toast.LENGTH_LONG)
                val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))

                if(!pref.getPreferenceString(Structure.instance.PREFS_MAC_ADDRESS).isEmpty()) {
                    //this.mac_address = ""
                    val i = Intent(this, DispositivosPareados::class.java)
                    startActivity(i)

                } else {
                    message("Nenhum dispositivo está selecionado no momento")
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_lampadas -> {
                setMACIntentActivity(ControleActivity::class.java)
            }
            R.id.nav_plantas -> {

            }
            R.id.nav_dispositivo -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun bluetoothRequestActivate() {
        //Solicitar para ativar o bluetooth
        val intentLigarBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intentLigarBluetooth, Structure.instance.REQ_BLUETOOTH_ENABLE)
    }

    fun encontraDispositivosPareados () {
        val searchPairedDevicesIntent = Intent(this, DispositivosPareados::class.java)
        startActivityForResult(searchPairedDevicesIntent, Structure.instance.SELECIONAR_DISP_PAREADOS);
    }

    //Checar se a conexão Bluetooth está ok
    fun isBtAdapterEnabled() : Boolean {
        try {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()

            if (btAdapter.isEnabled()) {
                return true
            }
        } catch (e: Exception) {
            if (e is DeadObjectException) {
                Log.e("Exception ", "DeadObjectException")
            }
            e.printStackTrace()
        }
        return false
    }

    fun connectToBluetoothAdapter(mac_address : String) {
        if(!mac_address.isEmpty()) {
            if (!btApplication.isBluetoothConnected()) {
                btApplication.setupBluetoothConnection(mac_address, this)
                Log.i("CONN", "Iniciando conexão Bt...")
            } else {
                Log.i("CONN", "Bt JÁ conectado...")
            }
        } else {
            Toast.makeText(this, "Nenhum endereço MAC selecionado...", Toast.LENGTH_LONG).show()
            //btSelecionarDisp.visibility = View.VISIBLE
        }
    }

    fun setMACIntentActivity(activity : Class<ControleActivity>) {
        if(btApplication.isBluetoothConnected()) {
            val i = Intent(this, activity)
            startActivity(i)
        } else {
            message("O adaptador bluetooth deve estar conectado !")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        val id = v!!.id

        when(id) {
            btSelecionarDisp.id -> {
                if (isBtAdapterEnabled()) {
                    encontraDispositivosPareados()
                } else {
                    bluetoothRequestActivate()
                }
            }
            btParearDisp.id -> {
                message("Conectando...")
                if (isBtAdapterEnabled()) {
                    val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))
                    mac_address = pref.getPreferenceString(Structure.instance.PREFS_MAC_ADDRESS)
                    connectToBluetoothAdapter(mac_address!!)
                } else {
                    bluetoothRequestActivate()
                }
            }
            cvLampadas.id -> {
                setMACIntentActivity(ControleActivity::class.java)
            }
            cvAlarme.id -> {
                if(alarmOn) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.app_name)
                    builder.setMessage("Deseja desligar o Alarme ?")
                    builder.setIcon(R.drawable.ic_alert)
                    builder.setPositiveButton("SIM", DialogInterface.OnClickListener { dialog, id ->
                        cvAlarme.setBackgroundColor(ContextCompat.getColor(this, R.color.ColorDarkBlue))
                        cvAlarme.isClickable = false
                        iconAlarme.setColorFilter(ContextCompat.getColor(this,  R.color.colorTextPrimary))
                        tvAlarme.text = "ALARME ON"

                        btApplication.sendData(Structure.instance.ALARM_OFF_BT_STRING.toByteArray())
                        alarmOn = false

//                        val pref = PreferencesData(getSharedPreferences(Structure.instance.PREFS_FILE, Context.MODE_PRIVATE))
//                        pref.savePreferenceString(Structure.instance.PREFS_ALARM, alarmOn!!)
                    })
                    builder.setNegativeButton("NÂO", DialogInterface.OnClickListener { dialog, id -> dialog.dismiss() })
                    val alert = builder.create()
                    alert.show()
                }
            }
            fabVoiceControl.id -> {
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                try {
                    startActivityForResult(i, Structure.instance.VOICE_CONTORL_CODE)
                } catch (a : ActivityNotFoundException) {
                    Toast.makeText(applicationContext, "Erro", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
