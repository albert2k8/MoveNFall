package com.example.detectordecaidas

import android.content.Context
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bundle = intent.extras
        val userID = bundle?.getString("userID")
        val userName = bundle?.getString("userName")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("userID", userID)
        prefs.putString("userName", userName)
        prefs.apply()

        setup(userName ?: "")
    }

    private fun setup(userName : String){

        title = "Menú principal"
        findViewById<TextView>(R.id.userNameTextView).text = userName

        val monitorActivityButton = findViewById<Button>(R.id.monitorActivityButton)
        val trainingDataButton = findViewById<Button>(R.id.trainingDataButton)
        val settingButton = findViewById<Button>(R.id.settingButton)
        val signOutButton = findViewById<Button>(R.id.signOutButton)

        monitorActivityButton.setOnClickListener {
            if(emergencyDataCheck()){
                showMonitorActivityActivity()
            }
            else{
                Toast.makeText(this, "Pulse en configuracón e\nintroduza el nombre y\nel número de emergencia", Toast.LENGTH_LONG).show()
                settingButton.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }

        trainingDataButton.setOnClickListener {
            if(userDataCheck()){
                showTrainingDataActivity()
            }
            else{
                Toast.makeText(this, "Pulse en configuracón e\nintroduza los datos de usuario\nedad, peso, altura y género", Toast.LENGTH_LONG).show()
                settingButton.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }

        settingButton.setOnClickListener {
            showSettingActivity(userName)
        }

        signOutButton.setOnClickListener {
            signOutSession()
        }

    }

    private fun emergencyDataCheck(): Boolean {

        val prefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)
        val name = prefs.getString("name", "")
        val mobile = prefs.getString("mobile", "")

        return name != "" && mobile != ""
    }

    private fun userDataCheck() : Boolean{

        val prefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)

        val age = prefs.getString("age", "")
        val height = prefs.getString("height", "")
        val weigh = prefs.getString("weigh", "")
        val gender = prefs.getString("gender", "")

        return age != "" && height != "" && weigh != "" && gender != ""
    }

    private fun showMonitorActivityActivity(){
        val intent = Intent(this, FallMonitoringActivity::class.java)
        startActivity(intent)
    }

    private fun showTrainingDataActivity(){
        val intent = Intent(this, TrainingSelectionActivity::class.java)
        startActivity(intent)
    }

    private fun showSettingActivity(userName : String){
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra("userName", userName)
        }
        startActivity(intent)
    }

    private fun signOutSession(){
        val filePrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        val personalPrefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE).edit()
        val settingsPrefs = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE).edit()

        filePrefs.clear()
        personalPrefs.clear()
        settingsPrefs.clear()

        filePrefs.apply()
        personalPrefs.apply()
        settingsPrefs.apply()

        FirebaseAuth.getInstance().signOut()
        showAuthActivity()
    }

    private fun showAuthActivity(){
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

}
