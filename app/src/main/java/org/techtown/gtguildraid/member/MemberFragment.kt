package org.techtown.gtguildraid.member

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.common.HeroBottomSheetDialog
import org.techtown.gtguildraid.databinding.DialogProfileEditBinding
import org.techtown.gtguildraid.databinding.FragmentMemberBinding
import org.techtown.gtguildraid.models.entities.Hero
import org.techtown.gtguildraid.repository.RoomDB

class MemberFragment : Fragment(), HeroBottomSheetDialog.BottomSheetListener {
    private var _binding: FragmentMemberBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialogBinding: DialogProfileEditBinding

    private lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    var database: RoomDB? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                binding.nickname.text = database!!.memberDao().me.name
                binding.guildName.text = pref.getString("guildName", "")
                binding.profileImage.setImageResource(
                    resources.getIdentifier(
                        "character_" + pref.getString(
                            "profileImage",
                            "knight"
                        ), "drawable", requireContext().packageName
                    )
                )
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberBinding.inflate(inflater, container, false)
        val view = binding.root

        database = RoomDB.getInstance(activity)
        pref = requireActivity().getSharedPreferences("pref", Activity.MODE_PRIVATE)
        editor = pref.edit()

        val me = database!!.memberDao().me
        val nickname = me.name
        val guildName = pref.getString("guildName", "")
        val profileImageName = pref.getString("profileImage", "knight")

        binding.nickname.text = nickname
        binding.guildName.text = guildName
        binding.profileImage.setImageResource(
            resources.getIdentifier(
                "character_$profileImageName",
                "drawable",
                requireContext().packageName
            )
        )

        binding.editButton.setOnClickListener { setDialogView() }

        //fragment 생성
        val memberCurrentFragment = MemberCurrentFragment()
        val memberResignedFragment = MemberResignedFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.childContainer, memberCurrentFragment).commit()
        binding.tabs.apply {
            addTab(newTab().setText("현재 멤버"))
            addTab(newTab().setText("탈퇴 멤버"))
            //tab 선택 통해 fragment 선택
            addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val position = tab.position
                    Log.d("tabSelected", "" + position)
                    var selected: Fragment? = null
                    if (position == 0) selected =
                        memberCurrentFragment else if (position == 1) selected =
                        memberResignedFragment
                    assert(selected != null)
                    childFragmentManager.beginTransaction().replace(R.id.childContainer, selected!!)
                        .commit()
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }

        return view
    }

    //profile 변경 dialog
    private fun setDialogView() {
        val dialog = Dialog(requireActivity())
        dialogBinding = DialogProfileEditBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        //dialog 설정
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.setLayout(width, height)
        dialog.show()


        val dialogGuildName = dialogBinding.guildName
        val dialogNickname = dialogBinding.nickname
        val profileImageInDialog = dialogBinding.profileImage
        val me1 = database!!.memberDao().me


        dialogNickname.setText(me1.name)
        dialogGuildName.setText(pref.getString("guildName", ""))
        profileImageInDialog.setImageResource(
            resources.getIdentifier(
                "character_" +
                        pref.getString("profileImage", "knight"),
                "drawable",
                requireContext().packageName
            )
        )
        profileImageInDialog.setOnClickListener {
            val bottomDialog = HeroBottomSheetDialog(this)
            bottomDialog.show(requireActivity().supportFragmentManager, "bottomSheetDialog")
        }

        dialogBinding.updateButton.setOnClickListener {
            val sGuildName = dialogGuildName.text.toString().trim { it <= ' ' }
            val sNickname = dialogNickname.text.toString().trim { it <= ' ' }
            if (sGuildName != "" && sNickname != "") {
                database!!.memberDao().update(me1.id, sNickname, me1.remark)
                binding.nickname.text = sNickname
                editor.putString("guildName", sGuildName)
                editor.apply()
                binding.guildName.text = sGuildName
                val newProfileString = pref.getString("profileImage", "knight")

                binding.profileImage.setImageResource(
                    resources.getIdentifier(
                        "character_" +
                                newProfileString, "drawable", requireContext().packageName
                    )
                )
                dialog.dismiss()
            } else {
                Toast.makeText(activity, "길드 이름 및 닉네임을 입력하세요", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onImageClicked(hero: Hero?) {
        if (hero == null) return

        editor.putString("profileImage", hero.englishName)
        dialogBinding.profileImage.setImageResource(
            resources.getIdentifier(
                "character_" + hero.englishName,
                "drawable",
                requireContext().packageName
            )
        )
    }
}