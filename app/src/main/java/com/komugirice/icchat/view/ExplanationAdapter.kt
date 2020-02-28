package com.komugirice.icchat.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.ExplanationActivity
import com.komugirice.icchat.databinding.*

class ExplanationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = 8

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == VIEW_TYPE_1) {
            return ExplanationView1Holder(
                ExplanationView1Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if(viewType == VIEW_TYPE_2) {
            return ExplanationView2Holder(
                ExplanationView2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if(viewType == VIEW_TYPE_3) {
            return ExplanationView3Holder(
                ExplanationView3Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if(viewType == VIEW_TYPE_4) {
            return ExplanationView4Holder(
                ExplanationView4Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if(viewType == VIEW_TYPE_5) {
            return ExplanationView5Holder(
                ExplanationView5Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if(viewType == VIEW_TYPE_6) {
            return ExplanationView6Holder(
                ExplanationView6Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if(viewType == VIEW_TYPE_7) {
            return ExplanationView7Holder(
                ExplanationView7Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ExplanationView8Holder(
                ExplanationView8Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position + 1
    }

    /**
     * onBindViewHolder
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }


    companion object {
        const val VIEW_TYPE_1 = 1
        const val VIEW_TYPE_2 = 2
        const val VIEW_TYPE_3 = 3
        const val VIEW_TYPE_4 = 4
        const val VIEW_TYPE_5 = 5
        const val VIEW_TYPE_6 = 6
        const val VIEW_TYPE_7 = 7
        const val VIEW_TYPE_8 = 8
    }

}

class ExplanationView1Holder(val binding: ExplanationView1Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView2Holder(val binding: ExplanationView2Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView3Holder(val binding: ExplanationView3Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView4Holder(val binding: ExplanationView4Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView5Holder(val binding: ExplanationView5Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView6Holder(val binding: ExplanationView6Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView7Holder(val binding: ExplanationView7Binding) : RecyclerView.ViewHolder(binding.root)
class ExplanationView8Holder(val binding: ExplanationView8Binding) : RecyclerView.ViewHolder(binding.root)

