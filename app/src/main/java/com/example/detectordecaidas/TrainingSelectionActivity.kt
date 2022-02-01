package com.example.detectordecaidas

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class TrainingSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_selection)

        setup()
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

    private fun setup(){

        title = "Selección de Actividades"

        val laddersButton = findViewById<Button>(R.id.laddersButton)
        val lieDownButton = findViewById<Button>(R.id.lieDownButton)
        val walkButton = findViewById<Button>(R.id.walkButton)
        val runButton = findViewById<Button>(R.id.runButton)
        val jumpsButton = findViewById<Button>(R.id.jumpsButton)
        val sitDownButton = findViewById<Button>(R.id.sitDownButton)
        val liftUpButton = findViewById<Button>(R.id.liftUpButton)
        val liftButton = findViewById<Button>(R.id.liftButton)
        val continuousButton = findViewById<Button>(R.id.continuousButton)
        val othersButton = findViewById<Button>(R.id.othersButton)
        val trainingBackButton = findViewById<Button>(R.id.trainingBackButton)

        laddersButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName0), getString(R.string.training0), getString(R.string.description0))
        }

        lieDownButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName1), getString(R.string.training1), getString(R.string.description1))
        }

        walkButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName2), getString(R.string.training2), getString(R.string.description2))
        }

        runButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName3), getString(R.string.training3), getString(R.string.description3))
        }

        jumpsButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName4), getString(R.string.training4), getString(R.string.description4))
        }

        sitDownButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName5), getString(R.string.training5), getString(R.string.description5))
        }

        liftUpButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName6), getString(R.string.training6), getString(R.string.description6))
        }

        liftButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName7), getString(R.string.training7), getString(R.string.description7))
        }

        continuousButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName8), getString(R.string.training8), getString(R.string.description8))
        }

        othersButton.setOnClickListener {
            showRecordDataActivity(getString(R.string.trainingName9), getString(R.string.training9), getString(R.string.description9))
        }

        trainingBackButton.setOnClickListener {
            showHomeActivity()
        }

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

    private fun showRecordDataActivity(training : String, trainingType : String, trainingDescription : String){
        val intent = Intent(this, RecordDataActivity::class.java).apply {
            putExtra("training", training)
            putExtra("trainingType", trainingType)
            putExtra("trainingDescription", trainingDescription)
        }
        startActivity(intent)
        finish()
    }

}