package com.letsclimbthis.esigtesttask.ui.signfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.letsclimbthis.esigtesttask.log
import java.io.File
//
//class AdapterDirectoryList (
//    private var itemList: List<File>,
//    private val outerClickHandler: OnDirectoryListItemClickListener
//    ) :
//    RecyclerView.Adapter<RecyclerView.ViewHolder>()
//    {
//
//        init {
//            setHasStableIds(true)
//        }
//
//        interface OnDirectoryListItemClickListener {
//            fun onListItemClick(viewClicked: View, itemIndexInList: Int)
//        }
//
//        inner class DirectoryItemViewHolder(binding: DirectoryListItemBinding) :
//            RecyclerView.ViewHolder(binding.root),
//            View.OnClickListener {
//
//            override fun onClick(p0: View?) {
//                p0?.let { outerClickHandler.onListItemClick(p0, adapterPosition) }
//            }
//        }
//
//        private lateinit var binding: DirectoryListItemBinding
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            binding = DirectoryListItemBinding
//                .inflate(LayoutInflater.from(parent.context), parent, false)
//            return DirectoryItemViewHolder(binding)
//        }
//
//        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
////            with(binding) {
////                directoryItem = itemList[position]
////                clickHandler = holder as DirectoryItemViewHolder
////            }
//        }
//
//        override fun getItemCount() = itemList.size
//
//        override fun getItemId(position: Int) = position.toLong()
//
//        override fun getItemViewType(position: Int) = position
//
//        fun updateDataSet() {
//
//            val b = itemList
//            for(i in itemList) log("${i.name}")
//
//            itemList = emptyList()
//            notifyDataSetChanged()
//
//            log("updateDataSet")
//            itemList = b
//            notifyDataSetChanged()
//
//
//
////            CoroutineScope(Job()).launch {
////                delay(1000)
////                withContext(Dispatchers.Main) {
////
////                }
////            }
//
////            for(i in itemList.indices) notifyItemChanged(i)
//        }
//
////        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
////            super.onViewRecycled(holder)
////            binding.directoryItem = null
////        }

//}