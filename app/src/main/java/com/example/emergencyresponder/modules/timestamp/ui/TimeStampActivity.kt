package com.example.emergencyresponder.modules.timestamp.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.emergencyresponder.databinding.ActivityTimeStampBinding


class TimeStampActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeStampBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeStampBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}



