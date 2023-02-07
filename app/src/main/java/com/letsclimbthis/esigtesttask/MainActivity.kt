package com.letsclimbthis.esigtesttask

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.letsclimbthis.esigtesttask.databinding.ActivityContainerRootBinding
import com.letsclimbthis.esigtesttask.domain.signature.CSP
import com.letsclimbthis.esigtesttask.ui.showlastcrash.UnexpectedCrashSaver

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityContainerRootBinding

    private val showCertificates = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContainerRootBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        binding.activityContainerChild.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

        setSupportActionBar(binding.activityContainerChild.toolbar)
        val drawerLayout: DrawerLayout = binding.activityContainerRootLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.menu_nav_sign_file,
                R.id.menu_nav_certificates,
                R.id.menu_nav_last_crash_report,
                ),
            drawerLayout,
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Thread.setDefaultUncaughtExceptionHandler(
                 UnexpectedCrashSaver(this)
        )

//        navView.setNavigationItemSelectedListener {
//            if (it.itemId == R.id.menu_install_cert)
//                showCertificates.launch(Intent("ru.cprocsp.intent.INSTALL_CERTIFICATE"))
//            true
//        }


//        permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
//        val filter = IntentFilter(ACTION_USB_PERMISSION)
//        registerReceiver(usbReceiver, filter)

    }

    override fun onStart() {
        super.onStart()
        CSP.init(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        doExitApp()
    }

    private var exitTime = 0L

    private fun doExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, "Press again to exit app", Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    private fun saveLog() {
        this.openFileOutput("log", Context.MODE_PRIVATE)
            .use {
                it.write(logMessage.toByteArray())
            }
    }

    override fun onPause() {
        super.onPause()
        saveLog()

    }

//    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
//    lateinit var device: UsbDevice
//    lateinit var permissionIntent: PendingIntent
//
//    private val usbReceiver = object : BroadcastReceiver() {
//
//        override fun onReceive(context: Context, intent: Intent) {
//            Toast.makeText(context, "BroadcastReceiver.onReceive", Toast.LENGTH_SHORT).show()
//            if (ACTION_USB_PERMISSION == intent.action) {
//
//                synchronized(this) {
//                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
//
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        device?.apply {
//
//
//                            val manager = getSystemService(Context.USB_SERVICE) as UsbManager
//                            manager.requestPermission(device, permissionIntent)
//
//
//
//                        }
//                    } else {
//                        Log.d("mytag", "permission denied for device $device")
//                    }
//                }
//            }
//        }
//    }


}