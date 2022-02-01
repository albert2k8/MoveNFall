package com.example.detectordecaidas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private const val minFallSensibility = 40
private const val maxFallSensibility = 90

private const val minImpactSensibility = 200
private const val maxImpactSensibility = 500

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bundle = intent.extras
        val userName = bundle?.getString("userName")

        setup(userName ?: "")
        loadPreviousUserData()
        loadPreviousSettings()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val userID = prefs.getString("userID", null)
        val userName = prefs.getString("userName", null)

        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("userID", userID)
            putExtra("userName", userName)
        }
        startActivity(intent)
    }

    private fun setup(userName : String){

        title = "Configuración"

        val fallSensibilityBar = findViewById<SeekBar>(R.id.fallSensibilityBar)
        val fallSensibilityValueText = findViewById<TextView>(R.id.fallSensibilityValueText)

        val impactSensibilityBar = findViewById<SeekBar>(R.id.impactSensibilityBar)
        val impactSensibilityValueText = findViewById<TextView>(R.id.impactSensibilityValueText)

        val saveSettingsButton = findViewById<Button>(R.id.saveSettingsButton)

        val uiToggleButton = findViewById<ToggleButton>(R.id.uiToggleButton)
        val gameToggleButton = findViewById<ToggleButton>(R.id.gameToggleButton)
        val fastestToggleButton =  findViewById<ToggleButton>(R.id.fastestToggleButton)

        var valueStr = ((maxFallSensibility + minFallSensibility)/2).toString() + "%"
        var value = (maxFallSensibility + minFallSensibility)/2 - minFallSensibility
        fallSensibilityValueText.text = valueStr
        fallSensibilityBar.max = maxFallSensibility - minFallSensibility
        fallSensibilityBar.progress = value
        fallSensibilityBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                valueStr = (minFallSensibility + p1).toString() + "%"
                fallSensibilityValueText.text = valueStr
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        valueStr = ((maxImpactSensibility + minImpactSensibility)/2).toString() + "%"
        value = (maxImpactSensibility + minImpactSensibility)/2 - minImpactSensibility
        impactSensibilityValueText.text = valueStr
        impactSensibilityBar.max = maxImpactSensibility - minImpactSensibility
        impactSensibilityBar.progress = value
        impactSensibilityBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                valueStr = (minImpactSensibility + p1).toString() + "%"
                impactSensibilityValueText.text = valueStr
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        uiToggleButton.setOnClickListener {
            uiToggleButton.isChecked = true
            gameToggleButton.isChecked = false
            fastestToggleButton.isChecked = false
            Toast.makeText(this,"Velocidad de muestreo baja seleccionada", Toast.LENGTH_SHORT).show()
        }

        gameToggleButton.setOnClickListener {
            uiToggleButton.isChecked = false
            gameToggleButton.isChecked = true
            fastestToggleButton.isChecked = false
            Toast.makeText(this,"Velocidad de muestreo media seleccionada", Toast.LENGTH_SHORT).show()
        }

        fastestToggleButton.setOnClickListener {
            uiToggleButton.isChecked = false
            gameToggleButton.isChecked = false
            fastestToggleButton.isChecked = true
            Toast.makeText(this,"Velocidad de muestreo alta seleccionada", Toast.LENGTH_SHORT).show()
        }

        saveSettingsButton.setOnClickListener {
            saveUserData(userName)
            saveSettings(userName)
            showHomeActivity()
        }

    }

    private fun loadPreviousUserData(){

        val prefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)
        val name = prefs.getString("name", "")
        val age = prefs.getString("age", "")
        val height = prefs.getString("height", "")
        val weigh = prefs.getString("weigh", "")
        val gender = prefs.getString("gender", "")
        val mobile = prefs.getString("mobile", "")

        findViewById<EditText>(R.id.addNameEditText).setText(name!!)
        findViewById<EditText>(R.id.addAgeEditText).setText(age!!)
        findViewById<EditText>(R.id.addHeightEditText).setText(height!!)
        findViewById<EditText>(R.id.addWeighEditText).setText(weigh!!)

        if(gender == "Masculino"){
            findViewById<RadioButton>(R.id.addMaleGenderButton).isChecked = true
            findViewById<RadioButton>(R.id.addFeminineGenderButton).isChecked = false
        }
        else if(gender == "Femenino"){
            findViewById<RadioButton>(R.id.addMaleGenderButton).isChecked = false
            findViewById<RadioButton>(R.id.addFeminineGenderButton).isChecked = true
        }
        else{
            findViewById<RadioButton>(R.id.addMaleGenderButton).isChecked = false
            findViewById<RadioButton>(R.id.addFeminineGenderButton).isChecked = false
        }

        findViewById<EditText>(R.id.addMobileEditText).setText(mobile!!)

    }

    private fun loadPreviousSettings(){

        val fallSensibilityBar = findViewById<SeekBar>(R.id.fallSensibilityBar)
        val fallSensibilityValueText = findViewById<TextView>(R.id.fallSensibilityValueText)

        val impactSensibilityBar = findViewById<SeekBar>(R.id.impactSensibilityBar)
        val impactSensibilityValueText = findViewById<TextView>(R.id.impactSensibilityValueText)

        val uiToggleButton = findViewById<ToggleButton>(R.id.uiToggleButton)
        val gameToggleButton = findViewById<ToggleButton>(R.id.gameToggleButton)
        val fastestToggleButton =  findViewById<ToggleButton>(R.id.fastestToggleButton)

        val prefs = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)

        var value = prefs.getString("fall", ((maxFallSensibility+minFallSensibility)/2-minFallSensibility).toString())
        var valueStr = "$value%"
        fallSensibilityValueText.text = valueStr
        fallSensibilityBar.progress = value!!.toInt() - minFallSensibility

        value = prefs.getString("impact", ((maxImpactSensibility+minImpactSensibility)/2-minImpactSensibility).toString())
        valueStr = "$value%"
        impactSensibilityValueText.text = valueStr
        impactSensibilityBar.progress = value!!.toInt() - minImpactSensibility

        uiToggleButton.isChecked = prefs.getString("ui", "true").toBoolean()
        gameToggleButton.isChecked = prefs.getString("game", "false").toBoolean()
        fastestToggleButton.isChecked = prefs.getString("fastest", "false").toBoolean()

    }

    private fun saveUserData(userName : String){

        val name = findViewById<EditText>(R.id.addNameEditText).text.toString()
        val age = findViewById<EditText>(R.id.addAgeEditText).text.toString()
        val height = findViewById<EditText>(R.id.addHeightEditText).text.toString()
        val weigh = findViewById<EditText>(R.id.addWeighEditText).text.toString()
        val gender = if(findViewById<RadioButton>(R.id.addMaleGenderButton).isChecked && !findViewById<RadioButton>(R.id.addFeminineGenderButton).isChecked){
            "Masculino"
        } else if(!findViewById<RadioButton>(R.id.addMaleGenderButton).isChecked && findViewById<RadioButton>(R.id.addFeminineGenderButton).isChecked){
            "Femenino"
        } else{
            ""
        }
        val mobile = findViewById<EditText>(R.id.addMobileEditText).text.toString()

        FirebaseFirestore.getInstance().collection(userName).document("userData").set(
            hashMapOf("name" to name, "age" to age, "height" to height, "weigh" to weigh, "gender" to gender, "mobile" to mobile)
        )

        val prefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE).edit()
        prefs.putString("name", name)
        prefs.putString("age", age)
        prefs.putString("height", height)
        prefs.putString("weigh", weigh)
        prefs.putString("gender", gender)
        prefs.putString("mobile", mobile)
        prefs.apply()

    }

    private fun saveSettings(userName : String){

        val fallSensibilityBar = findViewById<SeekBar>(R.id.fallSensibilityBar)
        val impactSensibilityBar = findViewById<SeekBar>(R.id.impactSensibilityBar)

        val uiToggleButton = findViewById<ToggleButton>(R.id.uiToggleButton)
        val gameToggleButton = findViewById<ToggleButton>(R.id.gameToggleButton)
        val fastestToggleButton =  findViewById<ToggleButton>(R.id.fastestToggleButton)

        val prefs = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE).edit()
        prefs.putString("fall", (minFallSensibility + fallSensibilityBar.progress).toString())
        prefs.putString("impact", (minImpactSensibility + impactSensibilityBar.progress).toString())
        prefs.putString("ui", uiToggleButton.isChecked.toString())
        prefs.putString("game", gameToggleButton.isChecked.toString())
        prefs.putString("fastest", fastestToggleButton.isChecked.toString())
        prefs.apply()

        FirebaseFirestore.getInstance().collection(userName).document("settings").set(
            hashMapOf("fall" to (minFallSensibility + fallSensibilityBar.progress).toString(),
                "impact" to (minImpactSensibility + impactSensibilityBar.progress).toString(),
                "ui" to uiToggleButton.isChecked.toString(),
                "game" to gameToggleButton.isChecked.toString(),
                "fastest" to fastestToggleButton.isChecked.toString())
        )

    }

    private fun showHomeActivity(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val userID = prefs.getString("userID", null)
        val userName = prefs.getString("userName", null)

        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("userID", userID)
            putExtra("userName", userName)
        }
        startActivity(intent)
        finish()
    }

}