package org.techtown.gtguildraid.member

import org.techtown.gtguildraid.models.entities.GuildMember
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import org.techtown.gtguildraid.R
import android.widget.TextView

class MemberCardAdapter(private val memberList: List<GuildMember?>?) :
    RecyclerView.Adapter<MemberCardAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = memberList?.get(position)
        if (member != null) {
            holder.setItem(member)
        }
    }

    override fun getItemCount(): Int {
        return memberList?.size ?: 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nickname: TextView
        var remark: TextView
        fun setItem(member: GuildMember) {
            nickname.text = member.name
            remark.text = member.remark
        }

        init {
            nickname = itemView.findViewById(R.id.nickname)
            remark = itemView.findViewById(R.id.remark)
        }
    }

    init {
        notifyDataSetChanged()
    }
}