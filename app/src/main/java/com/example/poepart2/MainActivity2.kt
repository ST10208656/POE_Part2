package com.example.poepart2

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.logging.Handler
import kotlin.collections.Collection

class MainActivity2 : AppCompatActivity() {
    private lateinit var firebaseDataManager: FirebaseDataManager
    lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    var userId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val user = intent.getStringExtra("Name")?: ""
        userId = intent.getStringExtra("UserId")?: ""
        println("user details are: $user , $userId")
        val welcomeUser = findViewById<TextView>(R.id.welcomeText)
        val refreshButton = findViewById<ImageButton>(R.id.refreshButton)
        val addCategoryButton = findViewById<Button>(R.id.addButton)
        val message = "Welcome $user"
        welcomeUser.text = message

        refreshButton.setOnClickListener{
            refreshItemList()
        }
        val textShader = LinearGradient(
            0f, 0f, welcomeUser.textSize * welcomeUser.text.length, welcomeUser.textSize,
            intArrayOf(resources.getColor(R.color.textStartColor), resources.getColor(R.color.textEndColor)),
            null, Shader.TileMode.CLAMP
        )
        welcomeUser.paint.shader = textShader

        firebaseDataManager = FirebaseDataManager()
        firebaseDataManager.readCategories(userId, object : FirebaseDataManager.DataStatus {
            override fun DataIsLoaded(category: Category) {}
            override fun DataIsLoaded(categories: MutableList<Category>) {
                println("Categories loaded: $categories")
                categoryAdapter.updateData(categories)

            }


            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
        })



        addCategoryButton.setOnClickListener {
            val bottomSheetDialog = AddCategoryBottomSheetDialog(userId)
            bottomSheetDialog.show(supportFragmentManager, "AddCategoryBottomSheetDialog")
        }

        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoryAdapter(mutableListOf())
        recyclerView.adapter = categoryAdapter

        categoryAdapter.onItemClick = { category ->
            val intent = Intent(this, CollectionsActivity::class.java)
            intent.putExtra("categoryId", category.id)
            intent.putExtra("userId", userId)// Pass category ID to CollectionsActivity
            startActivity(intent)
        }
        categoryAdapter.onDeleteClick = { category ->
            deleteCategory(category)
        }
    }
    private fun deleteCategory(category: Category) {
        val categoryId = category.id
        if (categoryId.isEmpty()) {
            Toast.makeText(this, "Category ID is empty", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseDataManager.deleteCategory(category)

        // Handle UI after deletion
        categoryAdapter.categories.remove(category)
        categoryAdapter.notifyDataSetChanged()

        Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
    }

    private fun refreshItemList() {
        firebaseDataManager.readCategories(userId,
            object : FirebaseDataManager.DataStatus{
                override fun DataIsLoaded(categories: MutableList<Category>) {
                    categoryAdapter.updateData(categories)
                }

                override fun DataIsLoaded(category: Category){}
                override fun DataIsInserted(){}
                override fun DataIsUpdated(){}
                override fun DataIsDeleted(){}
            })
    }
    private fun createCategoriesRecyclerView(categories: List<Category>) {
        categoryAdapter.updateData(categories)
    }
}

class CategoryAdapter(val categories: MutableList<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    var onItemClick: ((Category) -> Unit)? = null
    var onDeleteClick: ((Category) -> Unit)? = null

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView1)
        val deleteCategoryButton: ImageButton = itemView.findViewById(R.id.deleteCategoryButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        val layoutParams = ViewGroup.MarginLayoutParams(view.layoutParams)
        layoutParams.setMargins(30, 0, 30, 0)


        view.layoutParams = layoutParams
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                ContextCompat.getColor(parent.context, R.color.startColor),
                ContextCompat.getColor(parent.context, R.color.textStartColor)
            )
        )
        gradientDrawable.cornerRadius = 100f // Adjust corner radius as needed
        view.background = gradientDrawable
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameTextView.text = category.categoryName
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(category)
        }
        holder.deleteCategoryButton.setOnClickListener {
            onDeleteClick?.invoke(category)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    fun updateData(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    fun addCategory(category: Category) {
        categories.add(category)
        notifyItemInserted(categories.size - 1)
    }
}
