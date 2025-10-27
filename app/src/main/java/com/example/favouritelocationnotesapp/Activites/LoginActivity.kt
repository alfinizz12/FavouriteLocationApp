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

class LoginActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var loginBtn : Button
    private lateinit var emailTextField : EditText
    private lateinit var passwordTextField : EditText
    private lateinit var registerLinkNavigator : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Retrieve authentication infos like user email
        auth = Firebase.auth

        // Retrieve data
        emailTextField = findViewById<EditText>(R.id.emailLoginField)
        passwordTextField = findViewById<EditText>(R.id.passwordLoginField)
        registerLinkNavigator = findViewById<TextView>(R.id.loginLink)
        loginBtn = findViewById<Button>(R.id.loginBtn)

        // Buttons
        loginBtn.setOnClickListener {
            login(emailTextField.text.toString(), passwordTextField.text.toString())
        }

        registerLinkNavigator.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun login(email : String, password : String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Login success!.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        baseContext,
                        "Login failed!",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}