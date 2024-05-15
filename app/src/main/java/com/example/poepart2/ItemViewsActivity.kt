package com.example.poepart2

import android.content.pm.PackageManager
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

class ItemViewsActivity : AppCompatActivity() {
    private lateinit var firebaseDataManager: FirebaseDataManager
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_views)
val categoryGoalView = findViewById<TextView>(R.id.categoryGoalTextView)
        val itemId = intent.getStringExtra("itemId")?: ""
        val userId = intent.getStringExtra("userId")?: ""
        val categoryId = intent.getStringExtra("categoryId")?: ""
        val collectionId = intent.getStringExtra("collectionId")?: ""
        firebaseDataManager = FirebaseDataManager()
textView = findViewById<TextView>(R.id.itemNameTextView)
        firebaseDataManager.readItem(userId, categoryId, collectionId, itemId, object : FirebaseDataManager.ItemDataStatus {
            override fun DataIsLoaded(item: Item) {
                displayItemDetails(item)
            }
            override fun ItemsAreLoaded(items: MutableList<Item>) {}
    })

    }
    private fun displayItemDetails(item: Item) {

        textView.text = item.itemName
        findViewById<TextView>(R.id.itemDescriptionTextView).text = item.itemDescription
        findViewById<TextView>(R.id.itemDateTextView).text = item.itemDate

        // Load image
        val imageView = findViewById<ImageView>(R.id.itemImageView)
        val textShader = LinearGradient(
            0f, 0f, textView.textSize * textView.text.length, textView.textSize,
            intArrayOf(resources.getColor(R.color.textStartColor), resources.getColor(R.color.textEndColor)),
            null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader

        Glide.with(this)
            .load(item.itemImageUrl) // Assuming imageUrl is the URL of the image in Firebase Storage
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .into(imageView)
    }


}