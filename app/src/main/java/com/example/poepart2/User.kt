package com.example.poepart2
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date


@IgnoreExtraProperties
data class Category(
    var categoryName: String = "",
    var categoryGoal: Int = 0,
    @get:Exclude @set:Exclude var id: String = "",
    var collections: MutableList<Collection> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(categoryName)
        parcel.writeInt(categoryGoal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }

}
@IgnoreExtraProperties
data class Collection(
    var collectionName: String = "",
    @get:Exclude @set:Exclude var id: String = ""
)

@IgnoreExtraProperties
data class Item(
    var itemName: String = "",
    var itemDescription: String = "",
    var itemImageUrl: String = "",
    var itemDate: String = "",// Default value should be set
    @get:Exclude @set:Exclude var id: String = ""
)