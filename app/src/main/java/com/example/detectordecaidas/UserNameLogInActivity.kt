package com.example.detectordecaidas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

private const val genericDomain = "@detectordecaidas.com"

class UserNameLogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_name_log_in)

        setup()

    }

    private fun setup(){
        title = "Registrar nuevo usuario"

        val userKeyLogInBackButton = findViewById<Button>(R.id.userNameLogInBackAuthButton)
        val createUserKeyButton = findViewById<Button>(R.id.createUserKeyButton)

        createUserKeyButton.setOnClickListener {
            logInFunction()
        }

        userKeyLogInBackButton.setOnClickListener {
            showAuthActivity()
        }
    }

    private fun logInFunction(){

        val userNameEditText = findViewById<EditText>(R.id.newUserNameEditText)
        val passwordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val repPasswordEditText = findViewById<EditText>(R.id.repNewPasswordEditText)
        val pattern = Pattern.compile("[^a-zA-Z0-9]")
        val userName = userNameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val repPassword = repPasswordEditText.text.toString()

        val checks = arrayOf(false, false, false, false, false)

        if(userName.isEmpty()){
            userNameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.red))
            checks[0] = false
        }
        else{
            userNameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.black))
            checks[0] = true
        }

        // Comprobacion de que el email tiene un formato válido
        if(userName.isNotEmpty() && pattern.matcher(userName).find()){
            userNameEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
            checks[1] = false
        }
        else{
            userNameEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
            checks[1] = true
        }

        if(password.isEmpty()){
            passwordEditText.setHintTextColor(ContextCompat.getColor(this, R.color.red))
            checks[2] = false
        }
        else{
            passwordEditText.setHintTextColor(ContextCompat.getColor(this, R.color.black))
            checks[2] = true
        }

        if(repPassword.isEmpty()){
            repPasswordEditText.setHintTextColor(ContextCompat.getColor(this, R.color.red))
            checks[3] = false
        }
        else{
            repPasswordEditText.setHintTextColor(ContextCompat.getColor(this, R.color.black))
            checks[3] = true
        }

        if(password != repPassword){
            passwordEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
            repPasswordEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
            checks[4] = false
        }
        else{
            passwordEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
            repPasswordEditText.setTextColor(ContextCompat.getColor(this, R.color.black))
            checks[4] = true
        }

        // Comprobacion de todos los campos vaidos y erroneos.
        if(checks.count{it} == checks.size){
            val email = userName+genericDomain
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    showHomeActivity(it.result?.user?.uid ?: "", userName)
                    saveUserData(userName, password, it.result?.user?.uid ?: "")
                }
                else{
                    showAlert(it.exception.toString())
                }
            }
        }
        else{
            Toast.makeText(this, "Hay campos erróneos", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveUserData(userName:String, password:String, userID:String){

        val name = findViewById<EditText>(R.id.newNameEditText).text.toString()
        val age = findViewById<EditText>(R.id.newAgeEditText).text.toString()
        val height = findViewById<EditText>(R.id.newHeightEditText).text.toString()
        val weigh = findViewById<EditText>(R.id.newWeighEditText).text.toString()
        val mobile = findViewById<EditText>(R.id.newMobileEditText).text.toString()
        val gender = if(findViewById<RadioButton>(R.id.newMaleGenderButton).isChecked && !findViewById<RadioButton>(R.id.newFeminineGenderButton).isChecked){
            "Masculino"
        } else if(!findViewById<RadioButton>(R.id.newMaleGenderButton).isChecked && findViewById<RadioButton>(R.id.newFeminineGenderButton).isChecked){
            "Femenino"
        } else{
            ""
        }

        FirebaseFirestore.getInstance().collection(userName).document("userProfile").set(
            hashMapOf("userName" to userName, "password" to password, "userID" to userID)
        )

        FirebaseFirestore.getInstance().collection(userName).document("userData").set(
            hashMapOf("name" to name, "age" to age, "height" to height, "weigh" to weigh, "gender" to gender, "mobile" to mobile)
        )

        val prefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE).edit()
        prefs.putString("userName", name)
        prefs.putString("userID", userID)
        prefs.putString("name", name)
        prefs.putString("age", age)
        prefs.putString("height", height)
        prefs.putString("weigh", weigh)
        prefs.putString("gender", gender)
        prefs.putString("mobile", mobile)
        prefs.apply()
    }

    private fun showHomeActivity(userID : String, userName : String){
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("userID", userID)
            putExtra("userName", userName)
        }
        startActivity(intent)
        finish()
    }

    private fun showAuthActivity(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAlert(error : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error: $error\n")
        builder.setPositiveButton("Aceptar", null)

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }

}