package com.example.pichau.casainteligente

import android.os.Message

/**
 * Created by Pichau on 25/10/2017.
 */


interface BtMessageReceivedListener {
    fun receiveMessage(msg: String)
    fun stateProgress(state : Int)
}