package com.example.pichau.casainteligente

import android.app.Application
import android.content.Context
import android.os.Message
import android.util.Log

/**
 * Created by Henrique Dal Mora Rosendo da Silva on 25/10/2017.
 */

class BluetoothApplication : Application(), BtConnectionListener {
    override fun threadState(state: Int) {
        Log.i("BtConnectionListener", "ThreadState: $state")

        if (state == Structure.instance.STATE_THREAD_BEGIN) {
            btMessageReceived.stateProgress(Structure.instance.STATE_THREAD_BEGIN)
        } else {
            btMessageReceived.stateProgress(Structure.instance.STATE_THREAD_DONE)
        }
    }

    override fun validateMessage(msg: Message) {
        Log.i("BtConnectionListener", "Message Received in BluetoothApplication()")

        val bundle = msg.data
        val data = bundle.getByteArray("data")!!
        val dataString = String(data)

        if(!dataString.isEmpty()) {
            btMessageReceived.receiveMessage(dataString)
        }
    }

    private lateinit var btMessageReceived : BtMessageReceivedListener


    var connect : BluetoothConnectionThread? = null


    companion object {
        val instance : BluetoothApplication by lazy { BluetoothApplication() }
    }


    fun setupBluetoothConnection(address : String, btMessageReceived : BtMessageReceivedListener) {
        connect = BluetoothConnectionThread(this, address, this)
        connect!!.start()

        this.btMessageReceived = btMessageReceived

    }

    fun sendData(data : ByteArray) {
        if (connect != null)
            connect!!.write(data)
    }

    fun isBluetoothConnected() : Boolean {

        if (connect != null) {
            return connect!!.isConnected
        } else {
            return false
        }
    }

    fun closeBtConnection() {
        if(connect != null)
            connect!!.cancel()
    }
}