package org.techtown.gtguildraid.member

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.common.MySwipeHelper
import org.techtown.gtguildraid.databinding.FragmentMemberResignedBinding
import org.techtown.gtguildraid.models.entities.GuildMember
import org.techtown.gtguildraid.repository.RoomDB

class MemberResignedFragment : Fragment() {
    private var _binding: FragmentMemberResignedBinding? = null
    private val binding get() = _binding!!

    var memberList: MutableList<GuildMember?>? = ArrayList()
    var database: RoomDB? = null
    private lateinit var adapter: MemberCardAdapter
    val MAX_MEMBER = 29

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberResignedBinding.inflate(inflater, container, false)
        val view = binding.root

        database = RoomDB.getInstance(activity)
        memberList = database!!.memberDao().resignedMembers
        binding.resignedRecyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = MemberCardAdapter(memberList)
        binding.resignedRecyclerView.adapter = adapter

        binding.resignCnt.text = getString(R.string.member_numbers_resign, memberList!!.size)

        //swipeHelper를 통해 swipe 구현
        object : MySwipeHelper(activity, binding.resignedRecyclerView, 200) {
            //instaiateMyButton 구현을 통해 button 관련 정보를 삽입
            override fun instantiateMyButton(buffer: MutableList<MyButton>) {
                buffer.add(MyButton(
                    activity,
                    "삭제",
                    50,
                    0,
                    Color.parseColor("#FF0000")
                ) { pos: Int -> deleteMember(pos) })
                buffer.add(MyButton(
                    activity,
                    "복귀",
                    50,
                    0,
                    Color.parseColor("#FFFF00")
                ) { pos: Int -> recoverMember(pos) })
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

    private fun updateMember(pos: Int) {
        val m = memberList!![pos] ?: return
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
            database!!.memberDao().resignedMembers?.let { memberList!!.addAll(it) }
            binding.resignCnt.text = getString(R.string.member_numbers_resign, memberList!!.size)
            adapter.notifyDataSetChanged()
        }
    }

    private fun recoverMember(pos: Int) {
        val memberNum = database!!.memberDao().currentMembersWithoutMe.size
        if (memberNum == MAX_MEMBER) {
            showToast()
            return
        }
        val m = memberList!![pos] ?: return
        database!!.memberDao().setIsResigned(m.id, false)
        memberList!!.removeAt(pos)
        binding.resignCnt.text = getString(R.string.member_numbers_resign, memberList!!.size)
        adapter.notifyItemRemoved(pos)
        adapter.notifyItemRangeChanged(pos, memberList!!.size)
    }

    private fun deleteMember(pos: Int) {
        val m = memberList!![pos]
        database!!.memberDao().delete(m)
        memberList!!.removeAt(pos)
        binding.resignCnt.text = getString(R.string.member_numbers_resign, memberList!!.size)
        adapter.notifyItemRemoved(pos)
        adapter.notifyItemRangeChanged(pos, memberList!!.size)
    }

    private fun showToast() {
        Toast.makeText(activity, "인원이 가득찼습니다!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}