package com.kvsn.builds.cap1

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class CommentAdapter(private val comment: String) : BaseAdapter() {
    override fun getCount(): Int {
        return 0
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return null
    }
}
