package com.ygoto3.sample_android_network_service_discovery.tv

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket

const val TAG = "NSD"
const val PASSCODE = "0890"

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    private var nsdManager: NsdManager? = null
    private var servicePort: Int? = null
    private var serviceHost: InetAddress? = null

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String?) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.d(TAG, "Service discovery success. $serviceInfo")

            serviceInfo?.let {
                if (it.serviceName.contains("SampleAndroidNSD")) {
                    nsdManager?.resolveService(serviceInfo, resolveListener)
                }
            }
        }

        override fun onServiceLost(p0: NsdServiceInfo?) {
            TODO("Not yet implemented")
        }

        override fun onDiscoveryStopped(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun onStartDiscoveryFailed(p0: String?, p1: Int) {
            TODO("Not yet implemented")
        }

        override fun onStopDiscoveryFailed(p0: String?, p1: Int) {
            TODO("Not yet implemented")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(p0: NsdServiceInfo?, p1: Int) {
            Log.d(TAG, p1.toString())
        }

        override fun onServiceResolved(nsdServiceInfo: NsdServiceInfo?) {
            nsdServiceInfo?.let {
                servicePort = it.port
                serviceHost = it.host
            }

            servicePort?.let {
                val socket = Socket(serviceHost?.hostAddress, it)
                ClientSocketThread(socket).start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }

        AlertDialog.Builder(this)
            .setTitle("Sync your account to your phone?")
            .setMessage("You will be able to use the same data as your phone here on TV.")
            .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    setupNSD()
                }
            })
            .setNegativeButton("No", null)
            .show()
    }

    private fun setupNSD() {
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        nsdManager?.discoverServices("_witap8._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        AlertDialog.Builder(this)
            .setTitle("Passcode")
            .setMessage("Your passcode is $PASSCODE.")
            .setPositiveButton("OK", null)
            .show()
    }
}

class ClientSocketThread(private val socket: Socket) : Thread() {

    override fun run() {
        super.run()

        val pw = PrintWriter(socket.getOutputStream(), true)
        pw.println(PASSCODE)
    }

}