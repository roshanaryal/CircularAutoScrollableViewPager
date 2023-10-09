package com.roshanaryal.autoscrollableviewpager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.roshanaryal.circularautoscrollableviewpager.CircularAutoScrollableViewpager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewpager = findViewById<ViewPager2>(R.id.viewpager)

        CircularAutoScrollableViewpager.Builder<MyViewHolder,String>(viewpager,object :CircularAutoScrollableViewpager.OnCreateItemViewHolder<MyViewHolder>{
            override fun createViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.carousel_item,parent,false))
            }

        },object : CircularAutoScrollableViewpager.OnBindItemView<MyViewHolder,String>{
            override fun bindItemView(viewholder: MyViewHolder, item: String) {

            }

        }).Build().bind(getStringItemList())
    }

    private fun getStringItemList(): List<String> {
        val list = ArrayList<String>()

        for (i in 0..10){
            list.add(("This is $i item"))
        }
        return list
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}