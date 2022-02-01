package com.example.detectordecaidas

import android.Manifest.permission.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

private const val PERMISSION_REQUEST_CODE = 160292
private const val genericDomain = "@detectordecaidas.com"
private const val TAG = "probandoCosasDeLaApp"


class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()

        bundle.putString("message", "Integracion de Firebase completada")
        analytics.logEvent("InitScreen", bundle)

        applicationPermissionRequestFunction()
        setup()
        session()

    }

    override fun onStart() {
        super.onStart()
        val authLayout = findViewById<LinearLayout>(R.id.authLayout)
        authLayout.visibility = View.VISIBLE
    }

    private fun applicationPermissionRequestFunction(){
        val permissionSMS = ContextCompat.checkSelfPermission(this, SEND_SMS)
        val permissionWrite = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
        val permissionPhone = ContextCompat.checkSelfPermission(this, CALL_PHONE)
        val permissionFineLocation = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        val permissionCoarseLocation = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)

        if(permissionSMS == PackageManager.PERMISSION_DENIED || permissionWrite == PackageManager.PERMISSION_DENIED ||
            permissionPhone == PackageManager.PERMISSION_DENIED || permissionFineLocation == PackageManager.PERMISSION_DENIED ||
            permissionCoarseLocation == PackageManager.PERMISSION_DENIED){

            requestPermissions(arrayOf(SEND_SMS, WRITE_EXTERNAL_STORAGE, CALL_PHONE,
                ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    private fun setup(){
        title = "Autenticación"

        val signInButton = findViewById<Button>(R.id.signInButton)
        val userKeyLogInButton = findViewById<Button>(R.id.userNameLogInButton)

        signInButton.setOnClickListener {
            userNameSignInFunction()
        }

        userKeyLogInButton.setOnClickListener {
            showUserNameLogInActivity()
        }
    }

    private fun userNameSignInFunction(){
        val userNameEditText = findViewById<EditText>(R.id.userNameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val userName = userNameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val pattern = Pattern.compile("[^a-zA-Z0-9]")
        val checks = arrayOf(false, false, false)

        if(userName.isEmpty()){
            userNameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.red))
            checks[0] = false
        }
        else{
            userNameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.black))
            checks[0] = true
        }

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

        if(checks.count{it} == checks.size){
            val email = userName+genericDomain
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    loadSavedData(userName)
                    showHomeActivity(it.result?.user?.uid ?: "", userName)
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

    private fun session(){
        val authLayout = findViewById<LinearLayout>(R.id.authLayout)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val userID = prefs.getString("userID", null)
        val userName = prefs.getString("userName", null)

        if(userID != null && userName != null){
            authLayout.visibility = View.INVISIBLE
            showHomeActivity(userID, userName)
        }
    }

    private fun loadSavedData(userName: String) {
        FirebaseFirestore.getInstance().collection(userName).document("userData").get().addOnSuccessListener {
            val prefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE).edit()
            prefs.putString("name", it.get("name") as String?)
            prefs.putString("age",  it.get("age") as String?)
            prefs.putString("height",  it.get("height") as String?)
            prefs.putString("weigh",  it.get("weigh") as String?)
            prefs.putString("gender",  it.get("gender") as String?)
            prefs.putString("mobile",  it.get("mobile") as String?)
            prefs.apply()
        }

        FirebaseFirestore.getInstance().collection(userName).document("settings").get().addOnSuccessListener{
            val prefs = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE).edit()
            prefs.putString("fall",  it.get("fall") as String?)
            prefs.putString("impact",  it.get("impact") as String?)
            prefs.putString("ui",  it.get("ui") as String?)
            prefs.putString("game", it.get("game") as String?)
            prefs.putString("fastest", it.get("fastest") as String?)
            prefs.apply()
        }
    }

    private fun showUserNameLogInActivity(){
        val intent = Intent(this, UserNameLogInActivity::class.java)
        startActivity(intent)
    }

    private fun showHomeActivity(userID : String, userName : String){
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("userID", userID)
            putExtra("userName", userName)
        }
        startActivity(intent)
        finish()
    }

    private fun showAlert(error : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error:\n $error")
        builder.setPositiveButton("Aceptar", null)

        val dialog : AlertDialog = builder.create()
        dialog.show()
    }
}