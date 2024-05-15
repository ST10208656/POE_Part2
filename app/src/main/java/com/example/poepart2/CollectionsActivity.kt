package com.example.poepart2

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CollectionsActivity : AppCompatActivity() {
    private lateinit var selectedCategoryId: String
    private lateinit var category: Category
    private lateinit var firebaseDataManager: FirebaseDataManager
    private lateinit var textView: TextView
    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryGoal: String
    private lateinit var progressBarHorizontal: ProgressBar

    var selectedUserID: String = ""
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collections)

        recyclerView = findViewById(R.id.collectionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        collectionAdapter = CollectionAdapter(mutableListOf())
        recyclerView.adapter = collectionAdapter

        selectedUserID = intent.getStringExtra("userId") ?: ""
        val refreshButton = findViewById<ImageButton>(R.id.refreshButton)
        val addCollectionButton = findViewById<Button>(R.id.addCollectionButton)
        progressBarHorizontal = findViewById<ProgressBar>(R.id.progressBarHorizontal)
        val categoryGoalTextView = findViewById<TextView>(R.id.categoryGoalTextView)
        selectedCategoryId = intent.getStringExtra("categoryId") ?: ""
        firebaseDataManager = FirebaseDataManager()

        categoryGoal = ""
        firebaseDataManager.readCategory(selectedUserID, selectedCategoryId, object : FirebaseDataManager.DataStatus {
            override fun DataIsLoaded(categories: MutableList<Category>) {}

            override fun DataIsLoaded(category: Category) {
                this@CollectionsActivity.category = category
                categoryGoal = category.categoryGoal.toString()
                categoryGoalTextView.text = "Goal: $categoryGoal"
                updateUI(category) // Update UI after category is loaded
            }

            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })

        // Read collections for the category
        firebaseDataManager.readCollections(selectedUserID, selectedCategoryId, object : FirebaseDataManager.CollectionDataStatus {
            override fun DataIsLoaded(collection: Collection) {}
            override fun CollectionsAreLoaded(collections: MutableList<Collection>) {
                collectionAdapter.updateData(collections)
            }
        })

        addCollectionButton.setOnClickListener {
            val bottomSheetDialog = AddCollectionBottomSheetDialog(selectedUserID, selectedCategoryId)
            bottomSheetDialog.show(supportFragmentManager, "AddCollectionBottomSheetDialog")
        }

        refreshButton.setOnClickListener {
            refreshItemList()
        }

        firebaseDataManager.readItemCount(selectedUserID, selectedCategoryId, object : FirebaseDataManager.TotalItemsCallback {
            override fun onTotalItemsCounted(totalItems: Int) {
                println("Total items: $totalItems")
                progressBarHorizontal.progress = totalItems
            }
        })

        if (categoryGoal.isNotEmpty()) {
            progressBarHorizontal.max = categoryGoal.toInt()
        }
        val message = "Goal: $categoryGoal"

        // Set item click listener for RecyclerView items
        collectionAdapter.onItemClick = { collection ->
            // Start a new activity to add items to the selected collection
            val intent = Intent(this, ItemsActivity::class.java)
            intent.putExtra("collectionId", collection.id)
            intent.putExtra("collectionName", collection.collectionName)
            intent.putExtra("categoryId", selectedCategoryId) // Pass the collection ID to the new activity
            intent.putExtra("UserID", selectedUserID)
            intent.putExtra("CategoryGoal", categoryGoal)
            startActivity(intent)
        }
    }

    private fun refreshItemList() {
        firebaseDataManager.readCollections(selectedUserID,
            selectedCategoryId,
            object : FirebaseDataManager.CollectionDataStatus{
                override fun DataIsLoaded(collection: Collection) {}

                override fun CollectionsAreLoaded(collections: MutableList<Collection>) {

                    collectionAdapter.updateData(collections)
                }
            })
    }
    private fun updateUI(category: Category) {
        // Update UI with the category data
        textView = findViewById(R.id.textView1)
        textView.text = category.categoryName
        val textShader = LinearGradient(
            0f, 0f,  textView.textSize *  textView .text.length,  textView.textSize,
            intArrayOf(resources.getColor(R.color.textStartColor), resources.getColor(R.color.textEndColor)),
            null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader

    }
}
// CollectionAdapter.kt
class CollectionAdapter(private val collections: MutableList<Collection>) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {
    var onItemClick: ((Collection) -> Unit)? = null
    inner class CollectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionNameTextView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.collection_item, parent, false)
        val layoutParams = ViewGroup.MarginLayoutParams(view.layoutParams)
        layoutParams.setMargins(0, 0, 0, 60)

        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        val collection = collections[position]
        println("collection: $collection")
        holder.collectionNameTextView.text = collection.collectionName
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(collection)
        }
    }

    override fun getItemCount(): Int {
        return collections.size
    }
    fun updateData(newCollections: List<Collection>) {
        collections.clear()
        collections.addAll(newCollections)
        notifyDataSetChanged()
    }
}
