package com.example.pichau.casainteligente

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import android.widget.ProgressBar



/**
 * Created by Henrique Dal Mora Rosendo da Silva on 25/10/2017.
 */


class BluetoothConnectionThread() : Thread() {

    var btSocket : BluetoothSocket? = null
    var btServerSocket :  BluetoothServerSocket? = null
    var input : InputStream? = null
    var output : OutputStream? = null
    var btDevAddress : String? = null
    var myUUID : String = "00001101-0000-1000-8000-00805F9B34FB"
    var server : Boolean = false
    var running : Boolean = false
    var isConnected : Boolean = false

    var mContext : Context? = null

    private lateinit var btConnectionListener: BtConnectionListener

    init {
        this.server = true
    }

    constructor(context : Context, btDevAddress : String, btConnectionListener: BtConnectionListener) : this() {
        this.server = false
        this.btDevAddress = btDevAddress
        this.btConnectionListener = btConnectionListener

        this.mContext = context
    }


    override fun run() {
        Looper.prepare();

        //val progressBar = ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal)

        //progressBar.visibility = View.VISIBLE
        btConnectionListener.threadState(Structure.instance.STATE_THREAD_BEGIN)



        this.running = true
        val btAdapter : BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(this.server) {
            // Servidor

            try {
                btServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord("Super Counter", UUID.fromString(myUUID))
                btSocket = btServerSocket!!.accept()

                if (btSocket != null) {
                    btServerSocket!!.close()
                }

            } catch (e : IOException) {
                e.printStackTrace()
                toMainActivity("---N".toByteArray())
            }
        } else {
            // Cliente

            try {

                val btDevice : BluetoothDevice = btAdapter.getRemoteDevice(btDevAddress)
                btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(myUUID))

                btAdapter.cancelDiscovery()

                if(btSocket != null) {
                    btSocket!!.connect()
                }

            } catch (e : IOException) {
                e.printStackTrace()
                toMainActivity("---N".toByteArray())
            }
        }

        if (btSocket != null) {
            this.isConnected = true
            toMainActivity("---S".toByteArray())

            try {

                input = btSocket!!.inputStream
                output = btSocket!!.outputStream

                while(running) {

                    val buffer = ByteArray(1024)
                    var bytes : Int
                    var bytesRead : Int = -1

                    do {

                        bytes = input!!.read(buffer, bytesRead + 1, 1)
                        bytesRead += bytes

                    } while (buffer[bytesRead] != '\n'.toByte())

                    toMainActivity(Arrays.copyOfRange(buffer, 0, bytesRead))
                }

            } catch (e : IOException) {
                e.printStackTrace()
                toMainActivity("---N".toByteArray())
                this.isConnected = false
            }
        }

        btConnectionListener.threadState(Structure.instance.STATE_THREAD_DONE)
        Looper.loop()

    }

    fun write(data : ByteArray) {

        if(output != null) {
            try {

                Log.i("DATA: ", data.toString())
                output!!.write(data)

            } catch (e : IOException) {
                e.printStackTrace()
            }
        } else {
            toMainActivity("---N".toByteArray())
        }
    }

    fun cancel() {
        try {

            running = false
            this.isConnected = false
            btServerSocket!!.close()
            btSocket!!.close()

        } catch (e : IOException) {
            e.printStackTrace()
        }

        running = false
        this.isConnected = false
    }

    fun toMainActivity(data :ByteArray) {
        val message : Message? = Message()
        val bundle : Bundle? = Bundle()
        bundle!!.putByteArray("data", data)
        message!!.data = bundle

        btConnectionListener.validateMessage(message)

    }
}

