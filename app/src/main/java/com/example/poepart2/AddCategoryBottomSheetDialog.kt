package com.example.poepart2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AddCategoryBottomSheetDialog(userId: String) : BottomSheetDialogFragment() {
    private lateinit var firebaseDataManager: FirebaseDataManager
    private var userID: String = userId
    private lateinit var categoryAdapter: CategoryAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.category_popup_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseDataManager = FirebaseDataManager()
        view.findViewById<Button>(R.id.confirm_button1).setOnClickListener {
            val categoryName = view.findViewById<EditText>(R.id.category_name_edittext).text.toString()
            val categoryGoal = view.findViewById<EditText>(R.id.category_goal_edittext).text.toString().toIntOrNull()
            try {
                // Process the input (e.g., create a new category object)
                if (categoryName.isNotEmpty() && categoryGoal != null) {
                    val newCategory = Category()
                    newCategory.categoryName = categoryName
                    newCategory.categoryGoal = categoryGoal
                    firebaseDataManager.addCategory(userID, newCategory)
                    firebaseDataManager.readCategories(userID, object : FirebaseDataManager.DataStatus {
                        override fun DataIsLoaded(category: Category) {}
                        override fun DataIsLoaded(categories: MutableList<Category>) {
                            println("Categories loaded: $categories")
                            categoryAdapter = CategoryAdapter(categories)
                            categoryAdapter.updateData(categories)
                        }

                        override fun DataIsInserted() {}
                        override fun DataIsUpdated() {}
                        override fun DataIsDeleted() {}
                    })
                    dismiss()
                }else {
                    // Show error message or handle invalid input
                }
                // Add the new category to the adapter

            } catch(e: Exception) {
                println("There is an error $e")
            }
        }

        view.findViewById<Button>(R.id.cancel_button1).setOnClickListener {
            dismiss()
        }
    }
}