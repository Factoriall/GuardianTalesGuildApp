package org.techtown.gtguildraid.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.databinding.ActivityRegisterBinding
import org.techtown.gtguildraid.etc.HeroBottomSheetDialog
import org.techtown.gtguildraid.models.entities.GuildMember
import org.techtown.gtguildraid.models.entities.Hero
import org.techtown.gtguildraid.utils.RoomDB

class RegisterActivity : AppCompatActivity(), HeroBottomSheetDialog.BottomSheetListener {
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var profileImage: ImageView
    private lateinit var editor : SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editor = getSharedPreferences("pref", MODE_PRIVATE).edit()

        title = "내 프로필 등록"

        val nickname = binding.nickname
        val guildName = binding.guildName
        profileImage = binding.profileImage.apply{
            setImageResource(
                resources.getIdentifier("character_knight", "drawable", packageName)
            )
        }

        //bottomSheetDialog를 통해 이미지 클릭하게 설정, BottomSheetDialogFragment 사용
        profileImage.setOnClickListener {
            val dialog = HeroBottomSheetDialog(this)
            dialog.show(supportFragmentManager, "bottomSheetDialog")
        }

        //설정 끝날 시 sharedPreference 통해 저장
        val button = binding.button
        button.setOnClickListener {
            val cNickname = nickname.text.toString()
            val cGuildName = guildName.text.toString()
            if (cNickname.matches("".toRegex()) || cGuildName.matches("".toRegex())) {
                showToast("닉네임과 길드 이름을 적어주세요.")
                return@setOnClickListener
            }
            val database = RoomDB.getInstance(this@RegisterActivity)
            val member = GuildMember()
            member.name = cNickname
            member.remark = ""
            member.resigned = false
            member.me = true
            database.memberDao().insert(member)

            editor.putString("guildName", cGuildName)
            editor.putBoolean("isRegistered", true)
            editor.apply()
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    //back버튼 무효화
    override fun onBackPressed() {
        showToast("닉네임 및 길드 이름 삽입 후 시작 버튼을 눌러주세요")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    //onImageClick을 heroBottomSheetDialog에서 받아서 이미지 사진 바꾸기
    override fun onImageClicked(hero: Hero) {
        editor.putString("profileImage", hero.englishName)
        profileImage.setImageResource(
            resources.getIdentifier("character_" + hero.englishName, "drawable", packageName)
        )
    }
}