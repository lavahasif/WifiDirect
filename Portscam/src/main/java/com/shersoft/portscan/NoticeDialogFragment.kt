package com.shersoft.portscan

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shersoft.android_ip.util.ConnectedDevice
import com.shersoft.android_ip.util.ConnectedDevice.IDeviceConnected
import com.shersoft.android_ip.util.MyIp
import com.shersoft.portscan.adapter.CustomAdapter

class NoticeDialogFragment(var listener: NoticeDialogListener) : DialogFragment() {
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mCurrentLayoutManagerType: LayoutManagerType? = null
    private var mAdapter: CustomAdapter? = null
    private var myIp: MyIp? = null
    private var connectedDevice: ConnectedDevice? = null
    private var fragmentActivity: FragmentActivity? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myIp = MyIp(context)
        connectedDevice = ConnectedDevice(context)
    }

    override fun onDetach() {
        super.onDetach()
        myIp = null
        connectedDevice = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.content_portscan, container, false)


        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = rootView.findViewById<View>(R.id.recyclerview) as RecyclerView
        //        initDataset();

        mLayoutManager = LinearLayoutManager(activity)
        mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = savedInstanceState
                .getSerializable(KEY_LAYOUT_MANAGER) as LayoutManagerType?
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType)
        mAdapter = CustomAdapter(mDataset)
        mDataset = arrayOfNulls(10)
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView!!.adapter = mAdapter

        //        getListPort();
        fragmentActivity = requireActivity()
        return rootView
    }//public void run() {

    //        s = mDataset.length;
    val listIp: Unit
        get() {
//            myIp = MyIp(requireActivity())
//            connectedDevice = ConnectedDevice(requireActivity())
            val ipAddress_util = myIp?.getIPAddress_Util(true)
            //        s = mDataset.length;
            if (ipAddress_util != null) {
                ConnectedDevice(requireContext()).gethostData(
                    ipAddress_util,
                    object : IDeviceConnected {
                        override fun DeviceConnected(ip: String) {
                            mDataset[s] = ip
                            s++
                            requireActivity().runOnUiThread(Runnable {
                                mAdapter = CustomAdapter(mDataset)
                                mRecyclerView!!.adapter = mAdapter
                            } //public void run() {
                            )
                        }
                    })
            }
        }//public void run() {

    //        s = mDataset.length;
    val listPort: Unit
        get() {
//            myIp = MyIp((fragmentActivity)!!)
//            connectedDevice = ConnectedDevice((fragmentActivity)!!)
            val ipAddress_util = myIp?.getIPAddress_Util(true)
            //        s = mDataset.length;
            if (ipAddress_util != null) {
                connectedDevice?.gethostData(ipAddress_util, object : IDeviceConnected {
                    override fun DeviceConnected(ip: String) {
                        val stringStringMap = connectedDevice!!.portScan(ip, 1433, 500)
                        if ((stringStringMap["success"] == "true")) {
                            mDataset[s] = ip
                            s++
                            fragmentActivity!!.runOnUiThread(object : Runnable {
                                override fun run() {
                                    mAdapter = CustomAdapter(mDataset)
                                    mRecyclerView!!.adapter = mAdapter
                                } //public void run() {
                            })
                        }
                    }
                })
            }
        }
    var mDataset = arrayOf<String?>()
    private fun initDataset() {
        mDataset = arrayOfNulls(DATASET_COUNT)
        for (i in 0 until DATASET_COUNT) {
            mDataset[i] = "This is element #$i"
        }
    }

    fun setRecyclerViewLayoutManager(layoutManagerType: LayoutManagerType?) {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView!!.layoutManager != null) {
            scrollPosition =
                (mRecyclerView!!.layoutManager as LinearLayoutManager?)?.findFirstCompletelyVisibleItemPosition()
                    ?: 10
        }
        when (layoutManagerType) {
            LayoutManagerType.GRID_LAYOUT_MANAGER -> {
                mLayoutManager = GridLayoutManager(activity, SPAN_COUNT)
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
            }
            LayoutManagerType.LINEAR_LAYOUT_MANAGER -> {
                mLayoutManager = LinearLayoutManager(activity)
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
            else -> {
                mLayoutManager = LinearLayoutManager(activity)
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
        }
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.scrollToPosition(scrollPosition)
    }

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment?)
        fun onDialogNegativeClick(dialog: DialogFragment?)
    }

    companion object {
        var s = 0
        private val DATASET_COUNT = 60
        private val KEY_LAYOUT_MANAGER = "layoutManager"
        private val SPAN_COUNT = 2
    }
}

enum class LayoutManagerType {
    GRID_LAYOUT_MANAGER, LINEAR_LAYOUT_MANAGER
}
