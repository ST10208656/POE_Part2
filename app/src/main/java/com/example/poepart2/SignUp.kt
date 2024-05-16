package com.example.poepart2

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        val username = findViewById<EditText>(R.id.usernameText)
        val password = findViewById<EditText>(R.id.passwordText)
        val name = findViewById<EditText>(R.id.nameText)
        val surname = findViewById<EditText>(R.id.surnameText)
        val signupButton = findViewById<Button>(R.id.signupButton)
val signUpMessage = findViewById<TextView>(R.id.signupText)
        signupButton.setOnClickListener {
            val email = username.text.toString().trim()
            val pass = password.text.toString().trim()
            val user_name = name.text.toString().trim()
            val user_surname = surname.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty() || user_name.isEmpty() || user_surname.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val textShader = LinearGradient(
                0f, 0f, signUpMessage.textSize * signUpMessage.text.length, signUpMessage.textSize,
                intArrayOf(resources.getColor(R.color.textStartColor), resources.getColor(R.color.textEndColor)),
                null, Shader.TileMode.CLAMP
            )
            signUpMessage.paint.shader = textShader

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid ?: ""
                        val userMap = hashMapOf(
                            "email" to email,
                            "name" to user_name,
                            "surname" to user_surname
                        )

                        // Save user data
                        firestore.collection("Users")
                            .document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Signed Up Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

