package org.techtown.gtguildraid

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.techtown.gtguildraid.databinding.ActivityFirstBinding

class FirstActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirstBinding

    //splash 화면
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        //thread 실행을 통해 3초 동안 띄우게
        val welcomeThread: Thread = object : Thread() {
            override fun run() {
                try {
                    super.run()
                    sleep(3000) // Delay of 3 seconds
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val i = Intent(this@FirstActivity, MainActivity::class.java)
                    startActivity(i)
                    finish()
                }
            }
        }
        welcomeThread.start()
    }

    // back press 무력화, 렉 안 걸리게
    override fun onBackPressed() {
        // super.onBackPressed()
    }
}