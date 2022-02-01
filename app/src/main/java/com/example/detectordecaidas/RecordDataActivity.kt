package com.example.detectordecaidas

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

private const val GRAVITY = 9.8099

class RecordDataActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private var trainingInProcess : Boolean = false
    private lateinit var wakeLoc : PowerManager.WakeLock
    private lateinit var fileOut : File
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_data)

        val bundle = intent.extras
        val training = bundle?.getString("training")
        val trainingType = bundle?.getString("trainingType")
        val trainingDescription = bundle?.getString("trainingDescription")

        setup(training ?: "", trainingType ?: "", trainingDescription ?: "")
    }

    override fun onBackPressed() {
        if (trainingInProcess){
            Toast.makeText(this, "Pulse en parar actividad", Toast.LENGTH_SHORT).show()
        }
        else{
            super.onBackPressed()
            showTrainingDataActivity()
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.ENGLISH)

        val otherSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        otherSymbols.decimalSeparator = '.'
        otherSymbols.groupingSeparator = ','
        val df = DecimalFormat("###.#######", otherSymbols)

        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            fileOut.appendText("${dateFormat.format(Date())};${df.format(p0.values[0]/GRAVITY)};${df.format(p0.values[1]/GRAVITY)};${df.format(p0.values[2]/GRAVITY)}; ; ; \n")
        }

        if(p0?.sensor?.type == Sensor.TYPE_GYROSCOPE){
            fileOut.appendText("${dateFormat.format(Date())}; ; ; ;${df.format(p0.values[0])};${df.format(p0.values[1])};${df.format(p0.values[2])}\n")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun setup(training : String, trainingType : String, trainingDescription : String){

        title = "Realización de la actividad"

        val trainingTextView = findViewById<TextView>(R.id.trainingTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val recordingData = findViewById<TextView>(R.id.recordingDataTextView)
        val startRecordDataButton =  findViewById<Button>(R.id.startRecordDataButton)
        val stopRecordDataButton = findViewById<Button>(R.id.stopRecordDataButton)
        val backTrainingSelectionButton = findViewById<Button>(R.id.trainingRecordBackTrainingSelectionButton)

        val nameActivity = findViewById<EditText>(R.id.nameActivityEditText)


        if(trainingType == getString(R.string.training9)){
            nameActivity.visibility = View.VISIBLE
            nameActivity.hint = training
        }
        else{
            nameActivity.visibility = View.INVISIBLE
        }

        trainingTextView.text = training
        recordingData.text = getString(R.string.recordingStopped)
        descriptionTextView.text = getString(R.string.mobileLocation) +  trainingDescription
        recordingData.setTextColor(ContextCompat.getColor(this, R.color.redOff))

        startRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.greenOn))
        stopRecordDataButton.isEnabled = false
        stopRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.disable))

        backTrainingSelectionButton.setOnClickListener {
            showTrainingDataActivity()
        }

        startRecordDataButton.setOnClickListener {

            if(nameActivity.isVisible && nameActivity.text.isEmpty()){
                nameActivity.setHintTextColor(ContextCompat.getColor(this, R.color.red))
                Toast.makeText(applicationContext,"Introduzca el nombre de la actividad", Toast.LENGTH_SHORT).show()
            }
            else if(nameActivity.isVisible && nameActivity.text.isNotEmpty()){
                nameActivity.setHintTextColor(ContextCompat.getColor(this, R.color.black))
                nameActivity.isEnabled = false
                trainingInProcess = true
                startRecordDataButton.isEnabled = false
                startRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.disable))
                stopRecordDataButton.isEnabled = true
                stopRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.redOff))
                backTrainingSelectionButton.isEnabled = false
                backTrainingSelectionButton.setTextColor(ContextCompat.getColor(this, R.color.disable))

                initTrainingRecordData(nameActivity.text.toString(), trainingType)
            }
            else{
                trainingInProcess = true
                startRecordDataButton.isEnabled = false
                startRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.disable))
                stopRecordDataButton.isEnabled = true
                stopRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.redOff))
                backTrainingSelectionButton.isEnabled = false
                backTrainingSelectionButton.setTextColor(ContextCompat.getColor(this, R.color.disable))

                initTrainingRecordData(training, trainingType)
            }

/*

 */

        }

        stopRecordDataButton.setOnClickListener {
            uploadTrainingRecordData(trainingType)

            trainingInProcess = false
            startRecordDataButton.isEnabled = true
            startRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.greenOn))
            stopRecordDataButton.isEnabled = false
            stopRecordDataButton.setTextColor(ContextCompat.getColor(this, R.color.disable))
            backTrainingSelectionButton.isEnabled = true
            backTrainingSelectionButton.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

    }

    private fun initTrainingRecordData(training : String, trainingType : String){

        val recordingData = findViewById<TextView>(R.id.recordingDataTextView)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val dateNameFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.ENGLISH)

        val userPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val personalPrefs = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)
        val settingsPreferences = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)

        val userName = userPrefs.getString("userName", "")
        val age = personalPrefs.getString("age", "")
        val height = personalPrefs.getString("height", "")
        val weigh = personalPrefs.getString("weigh", "")
        val gender = personalPrefs.getString("gender", "")

        val normal = settingsPreferences.getString("ui", "true")
        val game = settingsPreferences.getString("game", "false")
        val fastest = settingsPreferences.getString("fastest", "false")

        val fileName = "${userName}_${dateNameFormat.format(Date())}_${trainingType}.csv"
        val path = getExternalFilesDir(null)
        val fileHeader = "TimeStamp;ax;ay;az;gx;gy;gz\n"

        var sensorDelay : Int = SensorManager.SENSOR_DELAY_UI
        var sensorDelayType = ""

        if(normal.equals("true")){
            sensorDelay = SensorManager.SENSOR_DELAY_UI
            sensorDelayType = "SENSOR_DELAY_UI"
        }
        else if(game.equals("true")){
            sensorDelay = SensorManager.SENSOR_DELAY_GAME
            sensorDelayType = "SENSOR_DELAY_GAME"
        }
        else if(fastest.equals("true")){
            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST
            sensorDelayType = "SENSOR_DELAY_FASTEST"
        }

        Toast.makeText(applicationContext, "Iniciando grabación de movimiento", Toast.LENGTH_SHORT).show()
        recordingData.text = getString(R.string.recordingData)
        recordingData.setTextColor(ContextCompat.getColor(this, R.color.greenOn))

        fileOut = File(path, fileName)

        fileOut.delete()
        fileOut.createNewFile()
        fileOut.appendText("# User ID $userName \n")
        fileOut.appendText("# Peso: $weigh - Altura: $height - Edad: $age - Genero: $gender\n")
        fileOut.appendText("# Fecha y hora del comienzo de la prueba ${dateFormat.format(Date())}\n")
        fileOut.appendText("# Tipo de actividad \"$training\" \n")
        fileOut.appendText("# Tipo de muestreo: $sensorDelayType \n")
        fileOut.appendText("# Caracterisitcas del dispositivo. Version ANDROID: ${android.os.Build.VERSION.SDK_INT} - Modelo: ${android.os.Build.MODEL} - Fabricante: ${android.os.Build.MANUFACTURER}\n")
        fileOut.appendText("# Acelerometro [g] --- Giroscopo [rad/s] \n")
        fileOut.appendText(fileHeader)

        wakeLoc = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::WakeLockTag").apply {
                acquire()
            }
        }

        // Registro de los sensores
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it,  sensorDelay, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
            sensorManager.registerListener(this, it,  sensorDelay, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun uploadTrainingRecordData(trainingType : String){

        val recordingData = findViewById<TextView>(R.id.recordingDataTextView)
        val uriFile = Uri.fromFile(fileOut)

        sensorManager.unregisterListener(this)
        wakeLoc.release()

        progressDialog = ProgressDialog(this);
        progressDialog.setMessage("Por favor, espere");
        progressDialog.setTitle("Cargando archivo");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);

        Firebase.storage.reference.child("${trainingType}/${uriFile.lastPathSegment}").putFile(uriFile).addOnCompleteListener{
            if(it.isSuccessful){
                progressDialog.dismiss()
                Toast.makeText(this, "Datos de la actividad guardados", Toast.LENGTH_SHORT).show()
                recordingData.text = getString(R.string.recordingStopped)
                recordingData.setTextColor(ContextCompat.getColor(this, R.color.redOff))

                val handler = Handler()
                handler.postDelayed(Runnable {
                    showTrainingDataActivity()
                }, 2000)
            }
            else{
                progressDialog.dismiss()
                showAlert(it.exception.toString())

                val handler = Handler()
                handler.postDelayed(Runnable {
                    showTrainingDataActivity()
                }, 2000)

            }
            fileOut.delete()
        }
    }

    private fun showTrainingDataActivity(){
        val intent = Intent(this, TrainingSelectionActivity::class.java)
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
