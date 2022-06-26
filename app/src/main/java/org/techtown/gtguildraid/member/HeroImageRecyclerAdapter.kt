package org.techtown.gtguildraid.member

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.models.entities.Hero

class HeroImageRecyclerAdapter(var heroes: List<Hero>, fragment: Fragment) :
    RecyclerView.Adapter<HeroImageRecyclerAdapter.ViewHolder>() {
    private val mListener: BottomSheetListener

    interface BottomSheetListener {
        fun onImageClicked(hero: Hero?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_image_selector, parent, false)
        val layoutParams = view.layoutParams
        layoutParams.width = parent.width / 3
        layoutParams.height = parent.width / 3
        view.layoutParams = layoutParams
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hero = heroes[position]
        holder.setItem(hero, mListener)
    }

    override fun getItemCount(): Int {
        return heroes.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var context: Context
        fun setItem(hero: Hero, mListener: BottomSheetListener) {
            imageView.setImageResource(
                context.resources.getIdentifier(
                    "character_" + hero.englishName,
                    "drawable",
                    context.packageName
                )
            )
            imageView.setOnClickListener { view: View? -> mListener.onImageClicked(hero) }
        }

        init {
            imageView = itemView.findViewById(R.id.profileImage)
            context = itemView.context
        }
    }

    init {
        mListener = fragment as BottomSheetListener
    }
}