package com.ncsu.imagc

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.ncsu.imagc.helpers.DriveServiceHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener, MenuItem.OnMenuItemClickListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        const val RC_SIGN_IN = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_settings, R.id.nav_historic), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setupGoogleAuth()
    }

    private fun setupGoogleAuth() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        setupMenu(account)
    }

    private fun setupMenu(account: GoogleSignInAccount?) {
        val navigationMenu: Menu = navView.menu
        val isLogged = account != null
        navigationMenu.findItem(R.id.nav_logout).isVisible = isLogged
        navigationMenu.findItem(R.id.nav_historic).isVisible = isLogged
        navigationMenu.findItem(R.id.nav_login).isVisible = !isLogged
        navigationMenu.findItem(R.id.nav_logout).setOnMenuItemClickListener(this)
        navigationMenu.performIdentifierAction(R.id.nav_home, 0)
        val headerView = navView.getHeaderView(0)
        account?.let {
            val nameTextView: TextView = headerView.findViewById(R.id.nameTextView)
            nameTextView.text = it.displayName
            val emailTextView: TextView = headerView.findViewById(R.id.emailTextView)
            emailTextView.text = it.email
            val imageView: ImageView = headerView.findViewById(R.id.imageView)
            imageView.setImageURI(it.photoUrl)
        }
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
        setupMenu(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)
            setupMenu(account)
            val credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account!!.account
            val googleDriveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName("AppName")
                .build()
            var mDriveServiceHelper = DriveServiceHelper(googleDriveService)
        } catch (e: ApiException) {
            setupMenu(null)
        }
    }

    override fun onMenuItemClick(p0: MenuItem): Boolean {
        when(p0.itemId) {
            R.id.nav_logout -> googleSignOut()
        }
        return false
    }

}