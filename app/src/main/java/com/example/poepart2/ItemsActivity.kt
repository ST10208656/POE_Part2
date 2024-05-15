package com.example.poepart2

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ItemsActivity : AppCompatActivity() {
    private lateinit var selectedCollectionId: String
    private lateinit var selectedCategoryId: String
    private lateinit var selectedUserId: String
    private lateinit var selectedCollectionName: String
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var textView: TextView
    private lateinit var firebaseDataManager: FirebaseDataManager

    private lateinit var categoryGoal: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)
        val refreshButton = findViewById<ImageButton>(R.id.refreshButton)
        recyclerView = findViewById(R.id.itemRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemAdapter = ItemAdapter(mutableListOf())
        recyclerView.adapter = itemAdapter
    categoryGoal = intent.getStringExtra("CategoryGoal")?: ""
        val addItemButton = findViewById<Button>(R.id.addItemButton)
        selectedCollectionId = intent.getStringExtra("collectionId") ?: ""
        selectedCategoryId = intent.getStringExtra("categoryId") ?: ""
        selectedUserId = intent.getStringExtra("UserID") ?: ""
        selectedCollectionName = intent.getStringExtra("collectionName") ?: ""
        textView = findViewById(R.id.textView3)
        textView.text = selectedCollectionName
        val textShader = LinearGradient(
            0f, 0f, textView.textSize * textView.text.length, textView.textSize,
            intArrayOf(resources.getColor(R.color.textStartColor), resources.getColor(R.color.textEndColor)),
            null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader

        firebaseDataManager = FirebaseDataManager()

        firebaseDataManager.readItems(selectedUserId,
            selectedCategoryId,
            selectedCollectionId,
            object : FirebaseDataManager.ItemDataStatus {
                override fun DataIsLoaded(item: Item) {}

                override fun ItemsAreLoaded(items: MutableList<Item>) {
                    itemAdapter.updateData(items)
                    retrieveNumOfItems(items)
                }
            })

        addItemButton.setOnClickListener {
            val bottomSheetDialog =
                AddItemBottomSheetDialog(selectedUserId, selectedCollectionId, selectedCategoryId)
            bottomSheetDialog.show(supportFragmentManager, "AddItemBottomSheetDialog")
        }
        refreshButton.setOnClickListener {
            refreshItemList()
        }

        // Set item click listener for RecyclerView items
        itemAdapter.onItemClick = { item ->
            // Start a new activity to view items
            val intent = Intent(this, ItemViewsActivity::class.java)
            intent.putExtra("itemId", item.id)
            intent.putExtra("categoryId", selectedCategoryId)
            intent.putExtra("collectionId", selectedCollectionId)
            intent.putExtra("userId", selectedUserId)
            startActivity(intent)

        }
    }
private fun retrieveNumOfItems(items: MutableList<Item>) {
    val count = items.count()
    val itemNumTextView = findViewById<TextView>(R.id.itemNumTextView)
    val message = "Item: $count"
    itemNumTextView.text = message

}
    private fun refreshItemList() {
        firebaseDataManager.readItems(selectedUserId,
            selectedCategoryId,
            selectedCollectionId,
            object : FirebaseDataManager.ItemDataStatus {
                override fun DataIsLoaded(item: Item) {}

                override fun ItemsAreLoaded(items: MutableList<Item>) {
                    itemAdapter.updateData(items)
                }
            })
        firebaseDataManager.readCategory(selectedUserId, selectedCategoryId, object: FirebaseDataManager.DataStatus {
            override fun DataIsLoaded(category:Category){

            }

            override fun DataIsLoaded(categories: MutableList<Category>) {}
            override fun DataIsDeleted() {}

            override fun DataIsInserted() {}

            override fun DataIsUpdated() {}
        })

    }

}

    class ItemAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
        // Item click listener interface
        var onItemClick: ((Item) -> Unit)? = null

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)

            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(items[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_item, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items[position]
            holder.itemNameTextView.text = item.itemName
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun updateData(newItems: List<Item>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

