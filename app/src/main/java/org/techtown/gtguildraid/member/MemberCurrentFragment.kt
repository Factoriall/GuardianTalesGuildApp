package org.techtown.gtguildraid.member

import android.app.Dialog
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.common.MySwipeHelper
import org.techtown.gtguildraid.databinding.FragmentMemberCurrentBinding
import org.techtown.gtguildraid.models.entities.GuildMember
import org.techtown.gtguildraid.repository.RoomDB

class MemberCurrentFragment : Fragment() {
    private var _binding : FragmentMemberCurrentBinding? = null
    private val binding get() = _binding!!

    var memberList: MutableList<GuildMember?>? = ArrayList()
    var database: RoomDB? = null
    var adapter: MemberCardAdapter? = null

    private val MAX_MEMBER = 29
    var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberCurrentBinding.inflate(inflater, container, false)
        val view = binding.root

        val createButton = binding.createButton
        database = RoomDB.getInstance(activity)
        toast = Toast.makeText(activity, null, Toast.LENGTH_SHORT)
        memberList = database!!.memberDao().currentMembersWithoutMe

        binding.currentCnt.text = getString(R.string.member_numbers_current, memberList!!.size + 1)
        binding.currentRecyclerView.layoutManager = LinearLayoutManager(activity)
        val adapter = MemberCardAdapter(memberList)
        binding.currentRecyclerView.adapter = adapter
        createButton.setOnClickListener { createMember() }

        object : MySwipeHelper(activity, binding.currentRecyclerView, 200) {
            override fun instantiateMyButton(buffer: MutableList<MyButton>) {
                buffer.add(MyButton(
                    activity,
                    "삭제",
                    50,
                    0,
                    Color.parseColor("#FF0000")
                ) { pos: Int ->
                    deleteMember(pos)
                    binding.currentCnt.text = getString(R.string.member_numbers_current, memberList!!.size + 1)
                })
                buffer.add(MyButton(
                    activity,
                    "탈퇴",
                    50,
                    0,
                    Color.parseColor("#FFFF00")
                ) { pos: Int -> resignMember(pos) })
                buffer.add(MyButton(
                    activity,
                    "수정",
                    50,
                    0,
                    Color.parseColor("#90ee90")
                ) { pos: Int -> updateMember(pos) })
            }
        }
        return view
    }

    private fun createMember() {
        if (memberList!!.size == MAX_MEMBER) {
            showToast("멤버 인원이 가득찼습니다.")
            return
        }
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_member)
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.setLayout(width, height)
        dialog.show()
        val name = dialog.findViewById<EditText>(R.id.name)
        val remark = dialog.findViewById<EditText>(R.id.remark)
        val updateButton = dialog.findViewById<Button>(R.id.updateButton)
        updateButton.setOnClickListener {
            val sName = name.text.toString().trim { it <= ' ' }
            val sRemark = remark.text.toString().trim { it <= ' ' }
            if (sName != "") {
                dialog.dismiss()
                val member = GuildMember()
                member.name = sName
                member.remark = sRemark
                member.resigned = false
                member.me = false
                database!!.memberDao().insert(member)
                memberList!!.clear()
                memberList!!.addAll(database!!.memberDao().currentMembersWithoutMe)
                binding.currentCnt.text =
                    getString(R.string.member_numbers_current, memberList!!.size + 1)
                adapter!!.notifyDataSetChanged()
            } else {
                showToast("이름을 입력하세요")
            }
        }
    }

    private fun updateMember(pos: Int) {
        val m = memberList!![pos]!!
        val sID = m.id
        val sName = m.name
        val sRemark = m.remark
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_member)
        val width = WindowManager.LayoutParams.MATCH_PARENT
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.setLayout(width, height)
        dialog.show()
        val name = dialog.findViewById<EditText>(R.id.name)
        val remark = dialog.findViewById<EditText>(R.id.remark)
        val updateButton = dialog.findViewById<Button>(R.id.updateButton)
        name.setText(sName)
        remark.setText(sRemark)
        updateButton.setOnClickListener {
            dialog.dismiss()
            val uName = name.text.toString().trim { it <= ' ' }
            val uRemark = remark.text.toString().trim { it <= ' ' }
            database!!.memberDao().update(sID, uName, uRemark)
            memberList!!.clear()
            memberList!!.addAll(database!!.memberDao().currentMembersWithoutMe)
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun resignMember(pos: Int) {
        val m = memberList!![pos]
        if (m != null) {
            database!!.memberDao().setIsResigned(m.id, true)
        }
        memberList!!.removeAt(pos)
        binding.currentCnt.text = getString(R.string.member_numbers_current, memberList!!.size + 1)

        //recyclerview의 경우 notify를 통해 adapter에게 pos가 삭제되었음을 명시해 이를 실제 ui에 처리
        adapter!!.notifyItemRemoved(pos)
        adapter!!.notifyItemRangeChanged(pos, memberList!!.size)
    }

    private fun deleteMember(pos: Int) {
        val m = memberList!![pos]
        database!!.memberDao().delete(m)
        memberList!!.removeAt(pos)
        adapter!!.notifyItemRemoved(pos)
        adapter!!.notifyItemRangeChanged(pos, memberList!!.size)
    }

    private fun showToast(msg: String) {
        toast!!.setText(msg)
        toast!!.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}