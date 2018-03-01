package com.example.pichau.casainteligente

import android.R.id.edit
import android.content.SharedPreferences



/**
 * Created by Henrique Dal Mora Rosendo da Silva on 25/10/2017.
 */


class PreferencesData {

    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    constructor(preferences: SharedPreferences)  {
        this.preferences = preferences
        this.editor = this.preferences!!.edit()
    }

    // Método genérico serve para salvar qualquer tipo de preferencia String
    // não só sua filial
    fun savePreferenceString(key: String, value: String) {
        this.editor!!.putString(key, value)
        this.editor!!.commit()
    }

    fun getPreferenceString(key: String): String {
        return this.preferences!!.getString(key, "")
    }

    fun deletePreferenceString(key: String) : Boolean {
        this.editor!!.remove(key);
        return this.editor!!.commit();
    }
}