package com.example.poepart2

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlin.properties.Delegates


class FirebaseDataManager {
    private var totalItem1: Int = 0
    private val categoriesRef: CollectionReference = FirebaseFirestore.getInstance().collection("Categories")
private val collectionsRef: CollectionReference = FirebaseFirestore.getInstance().collection("Collections")
    private val firestore: FirebaseFirestore = Firebase.firestore
    interface DataStatus {
        fun DataIsLoaded(categories: MutableList<Category>)
        fun DataIsLoaded(category: Category)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }
    interface TotalItemsCallback {
        fun onTotalItemsCounted(totalItems: Int)
    }

    fun readCategories(userID: String, dataStatus: DataStatus) {
        firestore.collection("Users").document(userID).collection("Categories").get()
            .addOnSuccessListener { result ->
                val categories = mutableListOf<Category>()
                for (document in result) {
                    val category = document.toObject(Category::class.java)
                    category.id = document.id
                    categories.add(category)
                }
                dataStatus.DataIsLoaded(categories)
            }
            .addOnFailureListener { exception ->
                println("error "+exception)
            }
    }
    fun readCategory(userID: String, categoryId: String, dataStatus: DataStatus) {
        firestore.collection("Users").document(userID).collection("Categories").document(categoryId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val category = documentSnapshot.toObject(Category::class.java)
                    category?.id = documentSnapshot.id
                    if (category != null) {
                        dataStatus.DataIsLoaded(category)
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("error "+exception)
            }
    }
    fun addCategory(userID: String, category: Category) {
        val userCategoriesRef = firestore.collection("Users").document(userID).collection("Categories")

        userCategoriesRef.add(category)
            .addOnSuccessListener { documentReference ->
                category.id = documentReference.id
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding category to Firestore", e)
            }
    }

    fun addCollection(userID: String, categoryId: String, collection: Collection) {
        firestore.collection("Users").document(userID).collection("Categories")
            .document(categoryId)
            .collection("Collections")
            .add(collection)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
    fun addItem(userID: String, categoryId: String, collectionId: String, item: Item, imageUri: Uri, adapter: ItemAdapter) {
        // Add the item to the "Items" collection under the specified category and collection
        val itemRef = firestore.collection("Users").document(userID).collection("Categories")
            .document(categoryId)
            .collection("Collections")
            .document(collectionId)
            .collection("Items")
            .document()

        // Save the item data with the image URL in Firestore
        itemRef.set(item)
            .addOnSuccessListener {
                val itemId = itemRef.id
                Log.d(TAG, "Item added to Firestore with ID: $itemId")
                // Now upload the image after the item has been added successfully
                uploadImage(itemId, imageUri)
                readItems(userID, categoryId, collectionId, object : ItemDataStatus {
                    override fun ItemsAreLoaded(items: MutableList<Item>) {
                        adapter.updateData(items)
                    }
                    override fun DataIsLoaded(item: Item) {}
                })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding item to Firestore", e)
            }
    }
    fun readItem(userID: String, categoryId: String, collectionId: String, itemId: String, dataStatus: ItemDataStatus) {
        firestore.collection("Users").document(userID).collection("Categories")
            .document(categoryId)
            .collection("Collections")
            .document(collectionId)
            .collection("Items")
            .document(itemId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val item = documentSnapshot.toObject(Item::class.java)
                    item?.id = documentSnapshot.id
                    item?.let { dataStatus.DataIsLoaded(it) }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting item", exception)
            }
    }

    private fun uploadImage(itemId: String, imageUri: Uri) {
        Log.d(TAG, "Uploading image for item ID: $itemId")

        val storageRef = FirebaseStorage.getInstance().reference
            .child("item_images/$itemId.jpg") // Ensure that the file name has an extension

        // Upload the image to Firebase Storage
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Image uploaded successfully: ${taskSnapshot.metadata?.path}")
                // Get the download URL of the image
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        Log.d(TAG, "Image URL: $imageUrl")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error getting image URL", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image to Firebase Storage", e)
            }
    }





    fun updateCategory(category: Category) {
        categoriesRef.document(category.id).set(category)
            .addOnSuccessListener {
                // Update successful
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    fun deleteCategory(category: Category) {
        categoriesRef.document(category.id).delete()
            .addOnSuccessListener {
                // Deletion successful
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    fun readCollections(userID: String, categoryId: String, dataStatus: CollectionDataStatus) {
        firestore.collection("Users").document(userID).collection("Categories")
            .document(categoryId)
            .collection("Collections")
            .get()
            .addOnSuccessListener { result ->
                val collections = mutableListOf<Collection>()
                for (document in result) {
                    val collection = document.toObject(Collection::class.java)
                    collection.id = document.id
                    collections.add(collection)
                }
                dataStatus.CollectionsAreLoaded(collections)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }


    fun readItems(userID: String, categoryId: String, collectionId: String, dataStatus: ItemDataStatus) {
        firestore.collection("Users").document(userID).collection("Categories")
            .document(categoryId)
            .collection("Collections")
            .document(collectionId)
            .collection("Items")
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    val item = document.toObject(Item::class.java)
                    item.id = document.id
                    items.add(item)
                }
                dataStatus.ItemsAreLoaded(items)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting items", exception)
            }
    }
    fun readItemCount(userID: String, categoryId: String, callback: TotalItemsCallback): Int {
// Replace "yourCollectionGroup" with the name of your collection group
        val collectionGroup = firestore.collection("Users")
            .document(userID)
            .collection("Categories")
            .document(categoryId)
            .collection("Collections")



        collectionGroup.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    totalItem1++
                    callback.onTotalItemsCounted(totalItem1)
                }
                // Use totalItems here
                println("Total items: $totalItem1")

            }
            .addOnFailureListener { exception ->
                // Handle any errors
                println("Error getting documents: $exception")
            }
       return totalItem1
    }


    interface CollectionDataStatus {
        fun CollectionsAreLoaded(collections: MutableList<Collection>)
        fun DataIsLoaded(collection: Collection)
    }

    interface ItemDataStatus {
        fun ItemsAreLoaded(items: MutableList<Item>)
        fun DataIsLoaded(item: Item)
    }


    companion object {
        private const val TAG = "FirebaseDataManager"
    }
}