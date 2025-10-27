package com.example.favouritelocationnotesapp.Activites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.favouritelocationnotesapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var registerBtn : Button
    private lateinit var emailTextField : EditText
    private lateinit var passwordTextField : EditText
    private lateinit var usernameTextField : EditText
    private lateinit var nameTextField : EditText
    private lateinit var phoneTextField : EditText
    private lateinit var loginLinkNavigator : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        emailTextField = findViewById<EditText>(R.id.emailRegisterField)
        passwordTextField = findViewById<EditText>(R.id.passwordRegisterField)
        usernameTextField = findViewById<EditText>(R.id.usernameRegisterField)
        nameTextField = findViewById<EditText>(R.id.nameRegisterField)
        phoneTextField = findViewById<EditText>(R.id.phoneRegisterField)
        loginLinkNavigator = findViewById<TextView>(R.id.loginLink)
        registerBtn = findViewById<Button>(R.id.registerBtn)

        // buttons
        registerBtn.setOnClickListener {
            register(
                emailTextField.text.toString(),
                passwordTextField.text.toString(),
                usernameTextField.text.toString(),
                nameTextField.text.toString(),
                phoneTextField.text.toString()
            )
        }

        loginLinkNavigator.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    private val db = FirebaseFirestore.getInstance()

    private fun register(email: String, password: String, username : String, name: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid

                    if (uid != null) {

                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "name" to name,
                            "phone" to phone,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Register success!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Register success but failed store data!", Toast.LENGTH_SHORT).show()
                            }
                    }

                } else {
                    Toast.makeText(this, "Register failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

}