package com.shersoft.wifidirect.Util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.shersoft.wifidirect.databinding.FragmentFirstBinding


class W2peer(val contexts: Context, val _binding: FragmentFirstBinding) {


    private val intentFilter = IntentFilter()
    open lateinit var mManager: WifiP2pManager
    lateinit var channel: WifiP2pManager.Channel

    init {


        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        mManager = contexts.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = mManager.initialize(contexts, Looper.getMainLooper(), null)
        val w2pBroadCast = W2pBroadCast()
        contexts.registerReceiver(w2pBroadCast, intentFilter)
    }

    private val peers = mutableListOf<WifiP2pDevice>()
    lateinit var device: WifiP2pDevice
    val config = WifiP2pConfig()

    @SuppressLint("MissingPermission")
    val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        print(peerList)
        if (refreshedPeers != peers) {
            if (refreshedPeers.size > 0) {
                device = peerList.deviceList.first()
                _binding.textviewFirst.setText(device.toString());
                config.deviceAddress = device.deviceAddress
                if (device != null)
                    channel?.also { channel ->

                        mManager?.connect(channel, config, object : ActionListener {

                            override fun onSuccess() {
                                //success logic
                                print("connected")

                            }

                            override fun onFailure(reason: Int) {
                                print("fail" + reason)
                            }
                        }
                        )
                    }
            }
            peers.clear()
            peers.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.


            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (peers.isEmpty()) {
            Log.d("TAG", "No devices found")
            return@PeerListListener
        }
    }

    @SuppressLint("MissingPermission")
    fun discoverPeers() {
        mManager.discoverPeers(channel, actionListener)
    }

    private val actionListener = object : ActionListener {

        override fun onSuccess() {
            // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
        }

        override fun onFailure(reason: Int) {
            Toast.makeText(
                contexts,
                "Connect failed. Retry.$reason",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        // Picking the first device found on the network.
        val devices = peers[0]

        val config = WifiP2pConfig().apply {
            deviceAddress = devices.deviceAddress
            wps.setup = WpsInfo.PBC
        }


        mManager.connect(channel, config, actionListener)
    }

    fun disconnect() {
        mManager.cancelConnect(channel, actionListener)
    }

    inner class W2pBroadCast : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)

                    when (state) {
                        WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                            // Wifi P2P is enabled
                        }
                        else -> {

                        }
                    }
                    print(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    mManager?.requestPeers(channel, peerListListener)
                    mManager.requestGroupInfo(channel, DeviceListListener);
                    Log.d("TAG", "P2P peers changed")
                    // The peer list has changed! We should probably do something about
                    // that.
                    print(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    print(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                    // Connection state changed! We should probably do something about
                    // that.

                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    print(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
                    intent.getParcelableExtra<WifiP2pDevice>(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                    )

                }
            }
        }
    }

    object DeviceListListener :
        WifiP2pManager.GroupInfoListener {


        override fun onGroupInfoAvailable(group: WifiP2pGroup?) {
            print(group)
        }

    }

}
