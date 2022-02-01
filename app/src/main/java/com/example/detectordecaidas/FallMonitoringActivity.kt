package com.example.detectordecaidas

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.*
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*


private const val REQUEST_CODE = 160292
private const val gravity : Double = 9.81
private const val channelID : String = "channelID"
private const val notificationId = 0

class FallMonitoringActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var wakeLoc : PowerManager.WakeLock

    private var sensorDelay : Int = SensorManager.SENSOR_DELAY_UI
    private var monitoringInProcess : Boolean = false
    private var currentTime : Long = 0
    private var fallSensitivity : Double = 0.0
    private var impactSensitivity : Double = 0.0
    private var falling : Boolean = false
    private var impact : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_monitoring)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setup()

    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            processingData(p0.values[0].toDouble(), p0.values[1].toDouble(), p0.values[2].toDouble())
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onBackPressed() {
        if(monitoringInProcess){
            Toast.makeText(this, "Pulse en detener la monitorización", Toast.LENGTH_SHORT).show()
        }
        else{
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
    }

    private fun setup(){

        title = "Monitorización de caídas"

        val startMonitoring = findViewById<Button>(R.id.startFallMonitoringButton)
        val stopMonitoring = findViewById<Button>(R.id.stopFallMonitoringButton)
        val fallMonitoringBack = findViewById<Button>(R.id.fallMonitoringBackButton)
        val fallMonitoring = findViewById<TextView>(R.id.fallMonitoringTextView)

        startMonitoring.setTextColor(ContextCompat.getColor(this, R.color.greenOn))
        stopMonitoring.isEnabled = false
        stopMonitoring.setTextColor(ContextCompat.getColor(this, R.color.disable))
        fallMonitoring.text = getString(R.string.stopMonitoring)
        fallMonitoring.setTextColor(ContextCompat.getColor(this, R.color.redOff))

        val settingsPreferences = getSharedPreferences(getString(R.string.settings_file), Context.MODE_PRIVATE)
        fallSensitivity = settingsPreferences.getString("fall", "40")!!.toDouble()
        impactSensitivity = settingsPreferences.getString("impact", "300")!!.toDouble()

        val ui = settingsPreferences.getString("ui", "true")
        val game = settingsPreferences.getString("game", "false")
        val fastest = settingsPreferences.getString("fastest", "false")
        when {
            ui.equals("true") -> {
                sensorDelay = SensorManager.SENSOR_DELAY_UI
            }
            game.equals("true") -> {
                sensorDelay = SensorManager.SENSOR_DELAY_GAME
            }
            fastest.equals("true") -> {
                sensorDelay = SensorManager.SENSOR_DELAY_FASTEST
            }
        }

        notificationBuilder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Detector de caidas")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Se ha detectado un caida.\nPor favor, abra la aplicación si desea cancelar el aviso"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        createNotificationChannel()


        startMonitoring.setOnClickListener {
            monitoringInProcess = true

            startMonitoring.isEnabled = false
            startMonitoring.setTextColor(ContextCompat.getColor(this, R.color.disable))
            stopMonitoring.isEnabled = true
            stopMonitoring.setTextColor(ContextCompat.getColor(this, R.color.redOff))
            fallMonitoringBack.isEnabled = false
            fallMonitoringBack.setTextColor(ContextCompat.getColor(this, R.color.disable))

            Toast.makeText(applicationContext, "Monitorización de caídas iniciada", Toast.LENGTH_SHORT).show()
            fallMonitoring.text = getString(R.string.startMonitoring)
            fallMonitoring.setTextColor(ContextCompat.getColor(this, R.color.greenOn))

            wakeLoc = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::WakeLockTag").apply {
                    acquire()
                }
            }
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
                sensorManager.registerListener(this, it,  sensorDelay, SensorManager.SENSOR_DELAY_UI)
            }
        }

        stopMonitoring.setOnClickListener {
            sensorManager.unregisterListener(this)
            wakeLoc.release()

            monitoringInProcess = false

            startMonitoring.isEnabled = true
            startMonitoring.setTextColor(ContextCompat.getColor(this, R.color.greenOn))
            stopMonitoring.isEnabled = false
            stopMonitoring.setTextColor(ContextCompat.getColor(this, R.color.disable))
            fallMonitoringBack.isEnabled = true
            fallMonitoringBack.setTextColor(ContextCompat.getColor(this, R.color.black))

            Toast.makeText(applicationContext, "Monitorización de caídas detenida", Toast.LENGTH_SHORT).show()
            fallMonitoring.text = getString(R.string.stopMonitoring)
            fallMonitoring.setTextColor(ContextCompat.getColor(this, R.color.redOff))
        }

        fallMonitoringBack.setOnClickListener {
            showHomeActivity()
        }
    }

    private fun processingData(ax : Double, ay : Double, az : Double){

        val module = sqrt(ax*ax + ay*ay + az*az)/gravity

        if(module <= fallSensitivity/100 && !falling){
            currentTime = System.currentTimeMillis() + 1000
            falling = true
        }
        else if(module >= impactSensitivity/100 && falling && System.currentTimeMillis() <= currentTime){
            impact = true
            sensorManager.unregisterListener(this)
            wakeLoc.release()
        }
        else if(falling && System.currentTimeMillis() > currentTime){
            falling = false
            impact = false
        }

        if(falling && impact){
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, notificationBuilder.build())
            }
            showFallDetectionMessage()
            falling = false
            impact = false
        }
    }

    private fun notifyFall(){

        if(isLocationEnabled()){
            getCurrentLocation()
        }
        else{
            sendSMS(Double.NaN, Double.NaN)
        }
        phoneCall()

        wakeLoc = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::WakeLockTag").apply {
                acquire()
            }
        }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it,  sensorDelay, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun getCurrentLocation(){
        val task = fusedLocationProviderClient.lastLocation

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }

        task.addOnSuccessListener {
            if(it != null){
                sendSMS(it.latitude, it.longitude)
            }
            else{
                getNewLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation(){
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 2
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            sendSMS(lastLocation.latitude, lastLocation.longitude)
        }
    }

    private fun sendSMS(latitude : Double, longitude : Double){
        val emergencyContactPreferences = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)
        val personalPreferences = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)
        val personalName = personalPreferences.getString("name", "")
        val mobile = emergencyContactPreferences.getString("mobile", "")

        val message : String = if(!latitude.isNaN() && !longitude.isNaN()){
            "$personalName ha sufrido una caida. https://www.google.es/maps?q=$latitude,$longitude"
        } else{
            "$personalName ha sufrido una caida"
        }
        SmsManager.getDefault().sendTextMessage(mobile, null, message, null, null)
    }

    private fun phoneCall(){
        val emergencyContactPreferences = getSharedPreferences(getString(R.string.user_file), Context.MODE_PRIVATE)
        val mobileNumber = emergencyContactPreferences.getString("mobile", "")
        val intent = Intent(Intent.ACTION_CALL)
        intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
        intent.data = Uri.parse("tel:$mobileNumber")
        startActivity(intent)
    }

    private fun isLocationEnabled() : Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showFallDetectionMessage(){

        val dialog = AlertDialog.Builder(this)
            .setTitle("Caída detectada")
            .setMessage("¿Desea cancelar el aviso?")
            .setPositiveButton("Aceptar") { _, _ ->
                notifyFall()
            }
            .setNegativeButton("Cancelar"){ _, _ ->
                Toast.makeText(applicationContext, "Aviso cancelado", Toast.LENGTH_SHORT).show()

                wakeLoc = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::WakeLockTag").apply {
                        acquire()
                    }
                }

                sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
                    sensorManager.registerListener(this, it,  sensorDelay, SensorManager.SENSOR_DELAY_UI)
                }
            }
            .create()

        dialog.setOnShowListener(object : DialogInterface.OnShowListener {

            private val AUTO_DISMISS_MILLIS = 60000

            override fun onShow(dialog: DialogInterface) {
                val defaultButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                val positiveButtonText = defaultButton.text

                object : CountDownTimer(AUTO_DISMISS_MILLIS.toLong(), 100) {
                    override fun onTick(millisUntilFinished: Long) {
                        defaultButton.text = String.format(Locale.getDefault(), "%s (%d)", positiveButtonText, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)+1)
                    }
                    override fun onFinish() {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                            notifyFall()
                        }
                    }
                }.start()
            }
        })
        dialog.show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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

}