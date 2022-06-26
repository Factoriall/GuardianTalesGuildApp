package org.techtown.gtguildraid.activities

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.adapters.StatisticPagerAdapter
import org.techtown.gtguildraid.databinding.ActivityStatisticRaidBinding
import org.techtown.gtguildraid.databinding.DialogPathBinding
import org.techtown.gtguildraid.utils.RoomDB
import java.text.SimpleDateFormat
import java.util.*

class StatisticRaidActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticRaidBinding
    var raidId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticRaidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        val dateFormat = "yy/MM/dd"
        val database = RoomDB.getInstance(this)

        val tabLayout = binding.tabs
        val viewPager = binding.viewPager
        raidId = intent.getIntExtra("raidId", 0)
        val raid = database.raidDao().getRaid(raidId)
        val raidName = binding.raidName
        val raidTerm = binding.raidTerm
        val raidThumbnail = binding.raidThumbnail
        raidName.text = raid.name
        raidTerm.text = SimpleDateFormat(dateFormat).format(raid.startDay) + "~" +
                SimpleDateFormat(dateFormat).format(getEndTime(raid.startDay))
        raidThumbnail.setImageResource(
            resources.getIdentifier(
                "character_" + raid.thumbnail,
                "drawable",
                packageName
            )
        )

        //pager 관련 어댑터
        val adapter = StatisticPagerAdapter(supportFragmentManager, lifecycle)
        adapter.setData(raidId)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2 //양옆 로드할 page 리미트 생성

        //path 정보를 담는 info 버튼
        val pathInfo = binding.pathInfo
        pathInfo.setOnClickListener {
            val di = Dialog(this)
            val dialogBinding = DialogPathBinding.inflate(layoutInflater)

            di.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
            )
            di.show()

            dialogBinding.okbutton.setOnClickListener { di.dismiss() }
        }
        val exitButton = findViewById<ImageView>(R.id.exitButton)
        exitButton.setOnClickListener { finish() }

        //ViewPager2와 TabLayout을 연결 및 tab 내용 설정
        TabLayoutMediator(
            tabLayout,
            viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            if (position == 0) tab.text = "순위표" else if (position == 1) tab.text =
                "개인별 기록" else if (position == 2) tab.text = "보스별 기록"
        }.attach()
        viewPager.isUserInputEnabled = false
    }

    private fun getEndTime(day: Date): Date {
        val end = Calendar.getInstance()
        end.time = day
        end.add(Calendar.DATE, 13)
        return end.time
    }
}