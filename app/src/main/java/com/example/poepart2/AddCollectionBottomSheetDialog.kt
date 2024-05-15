package com.example.poepart2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCollectionBottomSheetDialog(userId1: String, categoryId1: String): BottomSheetDialogFragment() {
    private lateinit var firebaseDataManager: FirebaseDataManager
    private var categoryId: String = categoryId1
    private var userId: String = userId1
    private lateinit var collectionAdapter: CollectionAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.collection_popup_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseDataManager = FirebaseDataManager()
        view.findViewById<Button>(R.id.confirm_button1).setOnClickListener {
            val collectionName = view.findViewById<EditText>(R.id.collection_name_edittext).text.toString()
            try {
                // Process the input (e.g., create a new category object)
                if (collectionName.isNotEmpty()) {
                    val newCollection = Collection(collectionName)
                    firebaseDataManager.addCollection(userId, categoryId, newCollection)
                    firebaseDataManager.readCollections(userId, categoryId, object : FirebaseDataManager.CollectionDataStatus {
                        override fun DataIsLoaded(collection: Collection) {}
                        override fun CollectionsAreLoaded(collections: MutableList<Collection>) {
                            println("Collections loaded: $collections")
                            collectionAdapter = CollectionAdapter(collections)
                            collectionAdapter.updateData(collections)

                        }

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