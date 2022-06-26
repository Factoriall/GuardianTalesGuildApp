package org.techtown.gtguildraid.activities

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.databinding.ActivityMainBinding
import org.techtown.gtguildraid.fragments.MemberFragment
import org.techtown.gtguildraid.fragments.RaidRenewalFragment
import org.techtown.gtguildraid.fragments.RecordFragment
import org.techtown.gtguildraid.fragments.StatisticRenewalFragment
import org.techtown.gtguildraid.utils.RoomDB
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var myToast: Toast? = null

    //RegisterActivity 이후로 fragment를 리셋시키는 용도로 넣음
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                val ft = supportFragmentManager.beginTransaction()
                val fragment = supportFragmentManager.findFragmentById(R.id.container)!!
                ft.detach(fragment)
                ft.attach(fragment)
                ft.commit()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        //필요한 fragment 초기화
        val memberFragment = MemberFragment()
        val raidFragment = RaidRenewalFragment()
        val recordFragment = RecordFragment()
        val statisticRenewalFragment = StatisticRenewalFragment()
        myToast = Toast.makeText(applicationContext, null, Toast.LENGTH_LONG)

        //sharedPreference를 통해 처음 register됐는지 확인, mode_private는 자신 앱에서만 사용하겠다는 의미
        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val isRegistered = pref.getBoolean("isRegistered", false)
        if (!isRegistered) {
            startActivityForResult(Intent(this@MainActivity, RegisterActivity::class.java), 101)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
            //RegisterActivity 시작 및 끝날 시의 애니메이션 삽입
        }
        val database = RoomDB.getInstance(this)

        /* BottomNavigationView
        getSupportFragmentManager().beginTransaction().replace를 통해 tab 내용을 바꿀 수 있음
        bottom_navigation에 menu 정보를 넣어 여기에 Tab 정보를 삽입할 수 있음
         */
        val bottomNavigationView = binding.bottomNavigation

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.memberTab -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, memberFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.raidTab -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, raidFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.recordTab -> {
                    if (!database.raidDao().isCurrentRaidExist(Date())) {
                        Toast.makeText(this@MainActivity, "레이드 정보를 입력하세요!", Toast.LENGTH_LONG)
                            .show()
                        return@setOnItemSelectedListener false
                    }
                    val raid = database.raidDao().getCurrentRaidWithBosses(Date())
                    val bosses = raid.getBossList()
                    var isElementAllExist = true
                    for (boss in bosses) {
                        if (boss.elementId == 0) {
                            isElementAllExist = false
                            break
                        }
                    }
                    if (!isElementAllExist) {
                        showToast("보스 속성 정보를 모두 입력하세요!")
                        return@setOnItemSelectedListener false
                    }
                    showToast("최근 기록: " + pref.getString("recentWrite", "없음"))
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, recordFragment).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.statisticTab -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, statisticRenewalFragment).commit()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        //처음에 보스 정보가 다 정해져있으면 raidTab을, 아니면 recordTab을 사용
        var isElementAllSelected = false
        if (database.raidDao().isCurrentRaidExist(Date())) {
            val raid = database.raidDao().getCurrentRaidWithBosses(Date())
            val bosses = raid.getBossList()
            var isElementAllExist = true
            for (boss in bosses) {
                if (boss.elementId == 0) {
                    isElementAllExist = false
                    break
                }
            }
            if (isElementAllExist) isElementAllSelected = true
        }
        val isStarted = (database.raidDao().isCurrentRaidExist(Date())
                &&
                database.raidDao().getCurrentRaid(Date()).startDay <= Date())
        if (isElementAllSelected && isStarted) {
            supportFragmentManager.beginTransaction().replace(R.id.container, recordFragment)
                .commit()
            bottomNavigationView.selectedItemId = R.id.recordTab
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.container, raidFragment)
                .commit()
            bottomNavigationView.selectedItemId = R.id.raidTab
        }

        //권한을 부여하는 library 사용.
        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {}
            override fun onPermissionDenied(deniedPermissions: List<String>) {
                showToast("권한 거부\n$deniedPermissions")
            }
        }
        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("권한 거부 시 레이드 정보를 종합해주는 엑셀 파일 출력이 안될 수 있습니다.\n\n[설정] > [권한] 에서 수동으로 권한 부여를 해주시길 바랍니다.")
            .setPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .check()
    }

    fun showToast(msg: String?) {
        if (myToast != null) myToast!!.cancel()
        myToast = Toast.makeText(applicationContext, null, Toast.LENGTH_SHORT).apply {
            setText(msg)
            show()
        }
    }

    //backPress 관련
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("종료하시겠습니까?")
            .setPositiveButton("네") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("아니오") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
}