package com.ygoto3.sample_android_network_service_discovery.phone

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.RuntimeException
import java.net.ServerSocket
import java.net.Socket

const val TAG = "NSD"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNSD()
    }

    private fun setupNSD() {
        val nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        val context = this
        val serviceSocketThread = ServiceSocketThread(nsdManager, object : AccountSyncListener {
            override fun onAccountSyncRequested(message: String) {
                Log.d(TAG, "onAccountSyncRequested: " + message)
                runOnUiThread(Runnable {
                    AlertDialog.Builder(context)
                        .setTitle("Sync your account to your TV?")
                        .setMessage("You have received a sync request from a TV with passcode $message.  Will you accept it?")
                        .setPositiveButton("Accept", null)
                        .setNegativeButton("Reject", null)
                        .show()
                })
            }
        })
        serviceSocketThread.start()
    }
}

class ServiceSocketThread(
    private val nsdManager: NsdManager,
    private val connectionListener: AccountSyncListener
) : Thread() {

    private var serverSocket: ServerSocket? = null
    private var nsdServiceName: String? = null

    private var nsdRegistrationListener = object : NsdManager.RegistrationListener {
        override fun onRegistrationFailed(p0: NsdServiceInfo?, p1: Int) {
            TODO("Not yet implemented")
        }

        override fun onUnregistrationFailed(p0: NsdServiceInfo?, p1: Int) {
            TODO("Not yet implemented")
        }

        override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo?) {
            nsdServiceName = nsdServiceInfo?.let { it.serviceName }
        }

        override fun onServiceUnregistered(p0: NsdServiceInfo?) {
            TODO("Not yet implemented")
        }
    }

    override fun run() {
        super.run()

        try {
            serverSocket = ServerSocket(1234)
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

        val localPort = serverSocket?.localPort
        val serviceInfo = NsdServiceInfo()
        serviceInfo.serviceName = "SampleAndroidNSD"
        serviceInfo.serviceType = "_witap8._tcp."
        localPort?.let { serviceInfo.port = it }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener)

        while (!Thread.currentThread().isInterrupted()) {
            var socket: Socket? = null
            try {
                socket = serverSocket?.accept()
                socket?.let { ClientSocketThread(it, connectionListener).start() }
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }
    }
}

class ClientSocketThread(private val socket: Socket, private val connectionListener: AccountSyncListener) : Thread() {
    private val inputReader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private val outputStream: OutputStream = socket.getOutputStream()

    override fun run() {
        super.run()

        while (!Thread.currentThread().isInterrupted()) {
            try {
                val data = inputReader.readLine() ?: throw IOException("Connection closed")
                Log.d(TAG, "data received: $data")
                connectionListener.onAccountSyncRequested(data)
            } catch (e: IOException) {
                break
            }
            Log.d(TAG, "Client socket closed: $socket")
        }
    }
}

interface AccountSyncListener {
    fun onAccountSyncRequested(message: String)
}