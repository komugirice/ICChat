package com.komugirice.icchat

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DemoActivity)
            adapter = Adapter(this@DemoActivity)
        }
    }


    class Adapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.demo_cell, null, false))

        override fun getItemCount(): Int = 100

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ViewHolder)
                holder.textView.text = "$position"
        }

        class ViewHolder(view : View): RecyclerView.ViewHolder(view) {
            val textView = view.findViewById<TextView>(R.id.textView)
        }
    }
}