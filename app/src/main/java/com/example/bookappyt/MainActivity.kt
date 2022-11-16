package com.example.bookappyt

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bookappyt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_main)
        // TODO check documentation on view binding on android docs
        setContentView(binding.root)

        // handle click, login
        binding.loginBtn.setOnClickListener {
            // will do later
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // handle click, skip and continue to main screen
        binding.skipBtn.setOnClickListener {
            // will do later
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }

        // now lets connect with firebase


    }
}