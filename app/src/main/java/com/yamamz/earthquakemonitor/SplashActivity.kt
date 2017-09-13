package com.yamamz.earthquakemonitor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import io.realm.Realm
import io.realm.RealmConfiguration

class SplashActivity : AppCompatActivity() {
var realm:Realm?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        try {
            val config = RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build()

            realm = Realm.getInstance(config)
            realm!!.close()
        }catch (e:Exception){

        }
        Handler().postDelayed(Runnable {

            val intent= Intent(SplashActivity@this,MainActivity::class.java)

            startActivity(intent)

            finish()

        },1000)
    }
}
