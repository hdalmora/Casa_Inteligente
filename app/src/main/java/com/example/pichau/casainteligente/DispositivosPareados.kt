package com.example.pichau.casainteligente

import android.app.Activity
import android.app.ListActivity
import android.content.Intent
import android.bluetooth.BluetoothDevice
import java.nio.file.Files.size
import android.widget.ArrayAdapter
import android.bluetooth.BluetoothAdapter
import android.widget.TextView
import android.view.LayoutInflater
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_dispositivos_pareados.*


/**
 * Created by Pichau on 25/10/2017.
 */

class DispositivosPareados : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val lv : ListView = listView
        val inflater : LayoutInflater = layoutInflater
        val header = inflater.inflate(R.layout.activity_dispositivos_pareados, tvPareados, false)
        val tv : TextView? = header.findViewById(R.id.tv_disp_pareados)
        tv!!.text = "Dispositivos pareados"
        lv.addHeaderView(header, null, false)

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = btAdapter.bondedDevices

        /*  Cria um modelo para a lista e o adiciona à tela.
            Se houver dispositivos pareados, adiciona cada um à lista.
         */
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        listAdapter = adapter
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                adapter.add(device.name + "\n" + device.address)
            }
        }
    }


    /*  Este método é executado quando o usuário seleciona um elemento da lista.
     */
    protected override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {

        /*  Extrai nome e endereço a partir do conteúdo do elemento selecionado.
            Nota: position-1 é utilizado pois adicionamos um título à lista e o
        valor de position recebido pelo método é deslocado em uma unidade.
         */
        val item = listAdapter.getItem(position - 1) as String
        val devName = item.substring(0, item.indexOf("\n"))
        val devAddress = item.substring(item.indexOf("\n") + 1, item.length)

        /*  Utiliza um Intent para encapsular as informações de nome e endereço.
            Informa à Activity principal que tudo foi um sucesso!
            Finaliza e retorna à Activity principal.
         */
        val returnIntent = Intent()
        returnIntent.putExtra("btDevName", devName)
        returnIntent.putExtra("btDevAddress", devAddress)
        setResult(Activity.RESULT_OK, returnIntent)
        Toast.makeText(this, "Dispositivo selecionado: $devName", Toast.LENGTH_LONG).show()
        finish()
    }

}