package com.example.poepart2

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = Firebase.firestore
    private lateinit var name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val username = findViewById<EditText>(R.id.editTextUsername)
        val password = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val signupButton = findViewById<Button>(R.id.signUpButton2)
        val usernameTest = findViewById<TextView>(R.id.usernameTestTextView)
val loginText = findViewById<TextView>(R.id.loginText)

        val textShader = LinearGradient(
            0f, 0f, loginText.textSize * loginText.text.length, loginText.textSize,
            intArrayOf(resources.getColor(R.color.textStartColor), resources.getColor(R.color.textEndColor)),
            null, Shader.TileMode.CLAMP
        )
        loginText.paint.shader = textShader
        name = ""
        loginButton.setOnClickListener {
            val username1 = username.text.toString()
            val password1 = password.text.toString()

            auth.signInWithEmailAndPassword(username1, password1)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userId = user.uid
                            val message = "$username1, $password1"
                            usernameTest.text = message
                            Toast.makeText(this, "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                            // Read user data from Firestore
                            firestore.collection("Users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        name = document.getString("name").toString()
                                        val surname = document.getString("surname")
                                        Toast.makeText(this, "Welcome $name $surname", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.d(TAG, "No such document")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d(TAG, "get failed with ", exception)
                                }
                            val intent = Intent(this, MainActivity2::class.java)
                            intent.putExtra("UserId", userId)
                            intent.putExtra("Name", name)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "Log In failed ", Toast.LENGTH_SHORT).show()
                    }
                }
        }
            signupButton?.setOnClickListener {
                val intent = Intent(this, SignUp::class.java)
                startActivity(intent)
            }
        }
    }


