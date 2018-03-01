package com.example.pichau.casainteligente

/**
 * Created by Pichau on 25/10/2017.
 */

class Structure {

    companion object {
        val instance : Structure by lazy { Structure() }
    }

    val ATIVAR_BLUETOOTH = 1
    val SELECIONAR_DISP_PAREADOS = 2
    val REQ_BLUETOOTH_ENABLE = 3
    val VOICE_CONTORL_CODE = 4

    val PREFS_FILE = "BtPrefsFile"
    val PREFS_MAC_ADDRESS = "mac_endereco"
    val PREFS_LED_1_ON = "led1_on"
    val PREFS_PWM_LED_1 = "led1_pwm"

    val STATE_THREAD_BEGIN = 0
    val STATE_THREAD_DONE = 1

    val LAMPADA_ON_BT_STRING = "N"
    val LAMPADA_OFF_BT_STRING = "E"
    val ALARM_OFF_BT_STRING = "K"

    var LAMPADA_ON_ARRAY  = arrayOf("ligar lâmpada",
    "ligar lâmpadas", "ligar a lâmpada", "ligar led", "ligar o led", "lâmpada on", "led on", "led 1", "lâmpada 1")

    var LAMPADA_OFF_ARRAY  = arrayOf("desligar lâmpada",
            "desligar lâmpadas", "desligar a lâmpada", "desligar led", "desligar o led", "lâmpada off", "LED off")



}