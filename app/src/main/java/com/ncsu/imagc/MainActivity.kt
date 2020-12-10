package com.ncsu.imagc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature
import com.microsoft.azure.storage.StorageUri
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.ncsu.imagc.data.AppDatabase
import com.ncsu.imagc.data.entities.PhotoInfo
import com.ncsu.imagc.data.entities.SensorInfo
import com.ncsu.imagc.data.entities.SensorValue
import com.ncsu.imagc.manager.SharedPreferencesManager
import com.ncsu.imagc.model.AzureStorageCredential
import com.ncsu.imagc.ui.dialogs.PhotoConditionsDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Math.round
import java.net.URI
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, MenuItem.OnMenuItemClickListener, SensorEventListener {

    private val credentials = listOf(
        AzureStorageCredential("?sv=2019-12-12&ss=bfqt&srt=sco&sp=rwdlacupx&se=2021-03-04T04:26:50Z&st=2020-12-03T20:26:50Z&spr=https&sig=OSokTwa6AE5O%2FGWEnj9Eptld%2B4KIwKlZom%2BfJfD3J%2F8%3D", StorageUri(URI("https://weedsapp.blob.core.windows.net/"))),
        AzureStorageCredential("?sv=2019-10-10&ss=bfqt&srt=sco&sp=rwdlacup&se=2021-03-04T08:45:02Z&st=2020-12-04T00:45:02Z&spr=https&sig=O2ud3EFHplFGlU2lPX6AOVVlu8lpYhYImbbs%2Bo6LQ98%3D", StorageUri(URI("https://weedsmedia.blob.core.usgovcloudapi.net/")))
    )

    private var azureContainers: List<CloudBlobContainer> = listOf()
    lateinit var db: AppDatabase
    private lateinit var sensorManager: SensorManager
    lateinit var sensorValues: MutableList<SensorValues>
    private var sensorTypes: MutableList<Int> = mutableListOf()
    lateinit var googleDriveService: Drive
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    var account: GoogleSignInAccount? = null
    var folderId: String = ""
    private var isUploading = false
    private var email: String = ""
    private var useAzureStorage = true

    //Conditions
    var weather: PhotoConditionsDialog.WeatherType = PhotoConditionsDialog.WeatherType.SUNNY
    var weedType: String = ""
    companion object {
        const val RC_SIGN_IN = 123
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_settings, R.id.nav_historic, R.id.nav_map), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        setupSettings()
        setupGoogleAuth()
        if(account != null) setupStorage()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_LOCATION)
        else
            setupLocationUpdates()
        startDatabase()
    }

    private fun setupGoogleAuth() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope(DriveScopes.DRIVE))
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        account = GoogleSignIn.getLastSignedInAccount(this)
        setupMenu()
    }

    private fun setupStorage() {
        val preferences: SharedPreferences = getSharedPreferences(SharedPreferencesManager.name, Context.MODE_PRIVATE)
        useAzureStorage = preferences.getBoolean(SharedPreferencesManager.SettingsPreferences.USE_AZURE.namePreference, true)
        if(useAzureStorage) setupAzure() else setupDrive()
    }

    private fun setupMenu() {
        val navigationMenu: Menu = navView.menu
        val isLogged = account != null
        navigationMenu.findItem(R.id.nav_logout).isVisible = isLogged
        navigationMenu.findItem(R.id.nav_historic).isVisible = isLogged
        navigationMenu.findItem(R.id.nav_map).isVisible = isLogged
        navigationMenu.findItem(R.id.nav_login).isVisible = !isLogged
        navigationMenu.findItem(R.id.nav_logout).setOnMenuItemClickListener(this)
        navigationMenu.performIdentifierAction(R.id.nav_home, 0)
        val headerView = navView.getHeaderView(0)
        account?.let {
            val nameTextView: TextView = headerView.findViewById(R.id.nameTextView)
            nameTextView.text = it.displayName
            val emailTextView: TextView = headerView.findViewById(R.id.emailTextView)
            emailTextView.text = it.email
            email = it.email ?: ""
            val imageView: ImageView = headerView.findViewById(R.id.imageView)
            imageView.setImageURI(it.photoUrl)
        }
    }

    fun goToLogin() {
        val navigationMenu: Menu = navView.menu
        navigationMenu.performIdentifierAction(R.id.nav_login, 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onClick(p0: View) {
        when(p0.id) {
            R.id.signInButton -> {
                googleSignIn()
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun googleSignOut() {
        mGoogleSignInClient.signOut()
        account = null
        setupMenu()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED)
                setupLocationUpdates()
            else
                finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        else {
            setupStorage()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        account = try {
            completedTask.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            null
        }
        setupMenu()
        setupStorage()
    }

    private fun setupDrive() {
        if (account == null)
            return
        val credential = GoogleAccountCredential.usingOAuth2(
            this, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account!!.account
        googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
        doAsync {
            try {
                val folders = googleDriveService.files().list().apply {
                    q = "name='WeedAI' and mimeType='application/vnd.google-apps.folder'"
                    spaces = "drive"
                }.execute()
                if (folders.files.size == 0) {
                    val fileMetadata = File()
                    fileMetadata.name = "WeedAI"
                    fileMetadata.mimeType = "application/vnd.google-apps.folder"

                        val folder = googleDriveService.files().create(fileMetadata).apply {
                            fields = "id"
                        }.execute()
                        folderId = folder.id

                } else {
                    folderId = folders.files[0].id
                }
                uiThread {
                    uploadImages()
                }
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, 20002)
            } catch (e: Exception) {
                Log.v("LXT", "KLXR")
            }
        }
    }

    fun addImage(imageUri: Uri, title: String, description: String, weedsAmount: String) {
        doAsync {
            try {
                val photoInfo = PhotoInfo(imageUri.path ?: "", title, description, weedsAmount, weather.name, weedType)

                location?.let {
                    photoInfo.latitude = it.latitude
                    photoInfo.longitude = it.longitude
                }
                val photoId = db.photoDao().insertPhoto(photoInfo)
                for(sensor in sensorValues.filter { it.actived }) {
                    val sensorId = db.photoDao().insertSensor(SensorInfo(sensor.type, sensor.unit, photoId))
                    val sensorValues = sensor.values.map { item -> SensorValue(item.toDouble(), sensorId) }
                    db.photoDao().insertValues(sensorValues)
                }
                uiThread {
                    setupStorage()
                }
            } catch (e: Exception) {
                Log.v("LXT", e.message ?: "")
            }
        }
    }

    private fun uploadImages() {
        if(isUploading)
            return
        isUploading = true
        if(useAzureStorage) uploadImagesByAzure() else uploadImagesByDrive()
    }

    private fun uploadImagesByDrive() {
        doAsync {
            try {
                val photosToSync = db.photoDao().getPhotos().filter { !it.synced }
                for (photo in photosToSync) {
                    val fileMetadata =
                        File()
                    fileMetadata.name = "${photo.name}.jpg"
                    fileMetadata.parents = Collections.singletonList(folderId)
                    val file = java.io.File(photo.photoUrl)
                    val mediaContent = FileContent("image/jpeg", file)

                    val fileTextMetadata = File()
                    fileTextMetadata.name = "${photo.name}.json"
                    fileTextMetadata.parents = Collections.singletonList(folderId)
                    val fileSensors = java.io.File(cacheDir, "${photo.name}.info")
                    var sensorInfo = "{'name': '${photo.name}'," +
                            "'description': '${photo.description}'," +
                            "'weather': '${photo.weather}'," +
                            "'weedType': '${photo.typeWeed}'," +
                            "'weedsAmount': '${photo.weedsAmount}'," +
                            "'email': '${email}'," +
                            "'sensors':["
                    val sensorData = db.photoDao().getSensors(photo.id)
                    for (sensor in sensorData) {
                        sensorInfo += "{'name':'${sensor.sensorName}','unit': '${sensor.units}', 'values': ${db.photoDao().getSensorValues(sensor.id).map{ it.value } }},"
                    }
                    sensorInfo += "]}"

                    fileSensors.writeText(sensorInfo)
                    val mediaContentText = FileContent("text/plain", fileSensors)

                    photo.fileId = googleDriveService.files().create(fileMetadata, mediaContent).apply {
                        fields = "id"
                    }.execute().id
                    photo.fileInfoId = googleDriveService.files().create(fileTextMetadata, mediaContentText).apply {
                        fields = "id"
                    }.execute().id
                    photo.synced = true
                    db.photoDao().updatePhoto(photo)
                    isUploading = false
                }

            } catch (e: Exception) {
                Log.v("LXT", "KLXR")
            }

        }
    }

    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when(p0.itemId) {
            R.id.nav_logout -> googleSignOut()
        }
        return false
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        sensorValues.find { it.sensor == event.sensor }?.values = event.values.toList()
    }

    override fun onResume() {
        super.onResume()
        registerListeners()
    }


    private fun registerListeners() {
        sensorValues.filter { it.actived }.forEach {
            sensorManager.registerListener(this, it.sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    fun getSensorInfo(showName: Boolean): String {
        return sensorValues.filter { it.actived }.joinToString(separator = "\n", transform = {
            it.getInfo(showName)
        })
    }

    @SuppressLint("MissingPermission")
    fun setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                location = locationResult.lastLocation
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun startDatabase() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).fallbackToDestructiveMigration().build()
    }

    private fun setupSettings() {
        sensorTypes = mutableListOf()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val preferences: SharedPreferences =
            getSharedPreferences(SharedPreferencesManager.name, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferences.edit()
        val mandatorySensors = listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE, Sensor.TYPE_PROXIMITY)
        val optionalSensors = listOf(Sensor.TYPE_GYROSCOPE)
        for(sensor in mandatorySensors) {
            if(preferences.contains(sensor.toString())) {
                sensorTypes.add(sensor)
            } else {
                if(sensorManager.getDefaultSensor(sensor) != null) {
                    editor.putBoolean(sensor.toString(), true)
                    sensorTypes.add(sensor)
                }
            }
        }
        for(sensor in optionalSensors) {
            if(preferences.contains(sensor.toString())) {
                sensorTypes.add(sensor)
            } else {
                if(sensorManager.getDefaultSensor(sensor) != null) {
                    editor.putBoolean(sensor.toString(), false)
                    sensorTypes.add(sensor)
                }
            }
        }
        editor.apply()

        sensorValues = mutableListOf()
        for(sensorType in sensorTypes) {
            sensorManager.getDefaultSensor(sensorType)?.let {
                sensorValues.add(SensorValues(it, preferences.getBoolean(it.type.toString(), false)))
            }
        }
    }

    class SensorValues(val sensor: Sensor, val actived: Boolean) {
        var values: List<Float> = listOf()
        var unit = when(sensor.type) {
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "m/s2"
            Sensor.TYPE_GYROSCOPE -> "rad/s"
            Sensor.TYPE_LIGHT -> "lux"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
            Sensor.TYPE_PRESSURE -> "hPa"
            Sensor.TYPE_PROXIMITY -> "cm"
            else -> ""
        }
        var type = when(sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "Accelerometer Uncalibrated"
            Sensor.TYPE_GYROSCOPE -> "Gyroscope"
            Sensor.TYPE_LIGHT -> "Light"
            Sensor.TYPE_AMBIENT_TEMPERATURE -> "Temperature"
            Sensor.TYPE_PRESSURE -> "Pressure"
            Sensor.TYPE_PROXIMITY -> "Proximity"
            else -> ""
        }
        fun getInfo(showName: Boolean): String {
            var info = "${type}: "
            if(showName)
                info = "${sensor.name} (${type}): "
            for(value in values) {
                info += "${round(value * 100) / 100} ${unit}  "
            }
            return info
        }

    }
    private fun setupAzure() {
        doAsync {
            try {
                azureContainers = listOf()
                for (credential in credentials) {
                    val accountSAS = StorageCredentialsSharedAccessSignature(credential.sasToken)
                    val blobClient = CloudBlobClient(credential.uri, accountSAS)
                    azureContainers = azureContainers.plus(blobClient.getContainerReference("images"))
                }
                uiThread {
                    uploadImages()
                }
            } catch (e: Throwable) {
                Log.v("LXT", e.message ?: "")
            }
        }
    }
    private fun uploadImagesByAzure() {
        doAsync {
            try {
                val photosToSync = db.photoDao().getPhotos()//.filter { !it.synced }
                for (photo in photosToSync) {

                    val file = java.io.File(photo.photoUrl)

                    val fileSensors = java.io.File(cacheDir, "${photo.name}.info")
                    var sensorInfo = "{'name': '${photo.name}'," +
                            "'description': '${photo.description}'," +
                            "'weather': '${photo.weather}'," +
                            "'weedType': '${photo.typeWeed}'," +
                            "'weedsAmount': '${photo.weedsAmount}'," +
                            "'latitude': '${photo.latitude}'," +
                            "'longitude': '${photo.longitude}'," +
                            "'email': '${email}'," +
                            "'sensors':["
                    val sensorData = db.photoDao().getSensors(photo.id)
                    for (sensor in sensorData) {
                        sensorInfo += "{'name':'${sensor.sensorName}','unit': '${sensor.units}', 'values': ${db.photoDao().getSensorValues(sensor.id).map{ it.value } }},"
                    }
                    sensorInfo += "]}"

                    fileSensors.writeText(sensorInfo)
                    for (azureContainer in azureContainers) {
                        val blob = azureContainer.getBlockBlobReference("${photo.name}.jpg")
                        blob?.uploadFromFile(file.path)
                        val blobSensor = azureContainer.getBlockBlobReference("${photo.name}.json")
                        blobSensor?.uploadFromFile(fileSensors.path)
                    }
                    photo.synced = true
                    db.photoDao().updatePhoto(photo)
                }
                isUploading = false

            } catch (e: Exception) {
                Log.v("LXT", "KLXR")
            }

        }
    }
}