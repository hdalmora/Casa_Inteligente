package com.example.pichau.casainteligente

import android.content.Context
import android.os.Message
import android.util.Log
import android.widget.Toast

/**
 * Created by Pichau on 25/10/2017.
 */


interface BtConnectionListener {
    fun validateMessage(msg: Message)

    fun threadState(state : Int)
}