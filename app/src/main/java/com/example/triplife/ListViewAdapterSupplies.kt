package com.example.triplife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.triplife.model.DataModelsupplies

class ListViewAdapterSupplies(val List: MutableList<DataModelsupplies>) : BaseAdapter() {
    override fun getCount(): Int {
        return List.size
    }

    override fun getItem(position: Int): Any {
        return List[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, converView: View?, parent: ViewGroup?): View {
        // coverView가 null이 아닐 경우 View를 재활용
        // 이 부분이 없다면, view를 리스트의 갯수만큼 호출해야 함
        var converView = converView
        if (converView == null) {
            // listview_item을 가져온다
            converView = LayoutInflater.from(parent?.context).inflate(R.layout.listview_itemsupplies, parent, false)
        }

        // List에 있는 데이터들을 하나씩 listview_item의 textView의 아이디를 찾아서 넣어줌
        val suppliesMemo = converView?.findViewById<TextView>(R.id.listViewArea)

        suppliesMemo!!.text = List[position].suppliesMemo


        return converView!!
    }


}