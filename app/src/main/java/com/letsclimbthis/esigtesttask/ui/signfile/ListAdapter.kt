package com.letsclimbthis.esigtesttask.ui.signfile

//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import com.letsclimbthis.esigtesttask.databinding.DirectoryListItemBinding
//import java.io.File
//
//class ListAdapter(
//    context: Context,
//    var items: List<File> = arrayListOf()
//) :
//    ArrayAdapter<File>(context, 0, items) {
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//
//        val binding = DirectoryListItemBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//
//        binding.directoryItem = items[position]
//
//        return binding.root
//    }
//
//    override fun getCount(): Int {
//        return items.size
//    }
//
//
//    fun submitList(items: List<File>) {
//        this.items = items
//        notifyDataSetChanged()
//    }
//}