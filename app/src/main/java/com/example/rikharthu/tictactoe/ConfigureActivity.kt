package com.example.rikharthu.tictactoe

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.rikharthu.tictactoe.tictac.Seed
import kotlinx.android.synthetic.main.activity_configure.*

class ConfigureActivity : AppCompatActivity() {

    val scaleDuration = 300L
    var selectedSeed = Seed.CROSS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)


        crossBtn.animate().scaleX(1.4f).scaleY(1.4f).duration = 0

        startBtn.setOnClickListener {
            val intent = Intent(this, TicTacActivity::class.java)
            intent.putExtra(TicTacActivity.KEY_PLAYER_SEED, selectedSeed)
            startActivity(intent)
        }

        crossBtn.setOnClickListener {
            if (selectedSeed != Seed.CROSS) {
                crossBtn.animate().scaleX(1.4f).scaleY(1.4f).duration = scaleDuration
                noughtBtn.animate().scaleX(1f).scaleY(1f).duration = scaleDuration
                crossBtn.isSelected = true
                noughtBtn.isSelected = false
                selectedSeed = Seed.CROSS
            }
        }
        noughtBtn.setOnClickListener {
            if (selectedSeed != Seed.NOUGHT) {
                noughtBtn.animate().scaleX(1.4f).scaleY(1.4f).duration = scaleDuration
                crossBtn.animate().scaleX(1f).scaleY(1f).duration = scaleDuration
                noughtBtn.isSelected = true
                crossBtn.isSelected = false
                selectedSeed = Seed.NOUGHT
            }
        }
    }
}
