package com.example.pichau.casainteligente

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_controle.*
import kotlinx.android.synthetic.main.content_controle.*

class ControleActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {

        when(v!!.id) {
            cvQuarto.id -> {
                if(BluetoothApplication.instance.isBluetoothConnected()) {
                    val i = Intent(this, LampadasActivity::class.java)
                    startActivity(i)
                } else {
                    Toast.makeText(this,"O adaptador bluetooth deve estar conectado !", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controle)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.setDisplayShowHomeEnabled(true);


        cvQuarto.setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here

        if(item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

}
