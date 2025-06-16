package com.example.myapplication.database

import androidx.compose.ui.input.key.key
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference // Added for clarity
import com.google.firebase.database.FirebaseDatabase // Added for clarity
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date // Keep if you use getStartDateAsDate/getEndDateAsDate helpers


class EventDB {

    companion object {
        var dbInstance = EventDB()
    }

    // This reference points to the root of your Firebase Realtime Database.
    // If your database is in a region other than 'us-central1', you need to provide the URL:
    private val databaseRoot = Firebase.database("https://eventapp-bf1ec-default-rtdb.europe-west1.firebasedatabase.app/").reference
//    private val databaseRoot: DatabaseReference = Firebase.database.reference

    private val activeItemsRef: DatabaseReference = databaseRoot.child("active_items")

    fun addActiveItem(item: ActiveItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val itemId = item.id.ifEmpty { // Check if the item already has an ID
            val newKey = activeItemsRef.push().key // Generate a new unique key
            if (newKey == null) {
                onFailure(Exception("Couldn't get push key for new item."))
                return
            }
            newKey // Use the new key as the ID
        }
        item.id = itemId // Assign the generated or existing ID to the item

        activeItemsRef.child(itemId).setValue(item)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getActiveItem(itemId: String, callback: (ActiveItem?) -> Unit) {
        activeItemsRef.child(itemId).get()
            .addOnSuccessListener { dataSnapshot ->
                try {
                    val item = dataSnapshot.getValue<ActiveItem>()
                    callback(item)
                } catch (e: Exception) {
                    // Handle potential deserialization errors, e.g., if data structure mismatch
                    callback(null)
                }
            }
            .addOnFailureListener {
                // Handle error (e.g., network issue, permissions)
                callback(null)
            }
    }

    fun getAllActiveItemsFlow(): Flow<List<ActiveItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<ActiveItem>()
                dataSnapshot.children.forEach { snapshot ->
                    try {
                        snapshot.getValue<ActiveItem>()?.let { item ->
                            items.add(item)
                        }
                    } catch (e: Exception) {
                        // Log or handle deserialization error for an individual item
                        println("Error deserializing item in flow: ${e.message}")
                    }
                }
                trySend(items).isSuccess // Offer the new list to the Flow
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Close the flow in case of error
                println("Database error for getAllActiveItemsFlow: ${databaseError.message}")
                close(databaseError.toException())
            }
        }

        activeItemsRef.addValueEventListener(listener)

        // When the Flow collector is cancelled, remove the listener
        awaitClose {
            activeItemsRef.removeEventListener(listener)
            println("Removed ValueEventListener for activeItemsRef")
        }
    }

    fun getAllActiveItems(callback: (List<ActiveItem>) -> Unit) {
        activeItemsRef.get()
            .addOnSuccessListener { dataSnapshot ->
                val items = mutableListOf<ActiveItem>()
                dataSnapshot.children.forEach { snapshot ->
                    try {
                        snapshot.getValue<ActiveItem>()?.let { item ->
                            items.add(item)
                        }
                    } catch (e: Exception) {
                        // Log error or handle individual item deserialization failure
                    }
                }
                callback(items)
            }
            .addOnFailureListener {
                // Handle error
                callback(emptyList())
            }
    }
}