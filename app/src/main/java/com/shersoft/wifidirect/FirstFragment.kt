package com.shersoft.wifidirect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.drjacky.imagepicker.ImagePicker
import com.shersoft.portscan.NoticeDialogFragment
import com.shersoft.wifidirect.Util.FileContainer
import com.shersoft.wifidirect.Util.FileUtil
import com.shersoft.wifidirect.Util.Utilss
import com.shersoft.wifidirect.Util.W2peer
import com.shersoft.wifidirect.databinding.FragmentFirstBinding
import kotlinx.coroutines.*
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var w2peer: W2peer
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        w2peer = activity?.let { W2peer(it?.applicationContext, _binding!!) }!!
        confirmFireMissiles()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.prev.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        binding.peer.setOnClickListener {
            print(Utilss.getIPAddress(true))
            w2peer.discoverPeers()
        }
        binding.disconnect.setOnClickListener {
            w2peer.disconnect()
        }
        binding.send.setOnClickListener {
//            FileServerAsyncTask(requireContext(), _binding!!.textviewStatus).execute()
            scope.launch { send2() }
        }
        binding.receive.setOnClickListener {
            receive()
        }
        binding.pick.setOnClickListener {
            launcher.launch(
                activity?.let { it1 ->
                    ImagePicker.with(it1)
                        //...
                        .cameraOnly() // or galleryOnly()
                        .createIntent()
                }
            )

        }
        binding.pick2.setOnClickListener {
            val data = Intent(Intent.ACTION_GET_CONTENT)
            data.addCategory(Intent.CATEGORY_OPENABLE)
            data.type = "*/*"
            val intent = Intent.createChooser(data, "Choose a file")

            launcher2.launch(intent)

        }
    }

    fun confirmFireMissiles() {
        val newFragment: DialogFragment =
            NoticeDialogFragment(object : NoticeDialogFragment.NoticeDialogListener {
                override fun onDialogPositiveClick(dialog: DialogFragment?) {
//                    TODO("Not yet implemented")
                }

                override fun onDialogNegativeClick(dialog: DialogFragment?) {
//                    TODO("Not yet implemented")
                }

            })
        newFragment.show(childFragmentManager, "missiles")

//        val dialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            AlertDialog.Builder(context)
//                .setView(R.layout.content_main)
//                .create()
//        } else {
//            AlertDialog.Builder(context)
//
//                .create()
//        }
//        dialog.show()
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                receivepicker(uri)
            }
        }
    private val launcher2 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                receivepicker(uri)
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    fun send() {
//        */
        var serverSocket = ServerSocket(8888, 3, InetAddress.getByName("192.168.49.103"))
//        try {
//            serverSocket.bind(InetSocketAddress.createUnresolved("192.168.49.103", 8888))
//        } catch (e: Exception) {
//            print(e)
//            serverSocket = ServerSocket(8888, 3, InetAddress.getByName("192.168.49.103"))
//        }
        print(serverSocket.inetAddress)
        return serverSocket.use {
            /**
             * Wait for client connections. This call blocks until a
             * connection is accepted from a client.
             */
            val client = serverSocket.accept()
            val inputstream = client.getInputStream()

            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */
            val f = File(
                Environment.getExternalStorageDirectory().absolutePath +
                        "/wifip2pshared-${System.currentTimeMillis()}.jpg"
            )
            val dirs = File(f.parent)

            dirs.takeIf { it.doesNotExist() }?.apply {
                mkdirs()
            }
            f.createNewFile()

            copyFile(inputstream, FileOutputStream(f))
            serverSocket.close()
            f.absolutePath
        }
    }

    fun send2() {
//        */
        var serverSocket = ServerSocket(8888, 3, InetAddress.getByName("192.168.49.103"))
//        try {
//            serverSocket.bind(InetSocketAddress.createUnresolved("192.168.49.103", 8888))
//        } catch (e: Exception) {
//            print(e)
//            serverSocket = ServerSocket(8888, 3, InetAddress.getByName("192.168.49.103"))
//        }
        print(serverSocket.inetAddress)
        return serverSocket.use {
            /**
             * Wait for client connections. This call blocks until a
             * connection is accepted from a client.
             */
            val client = serverSocket.accept()
            val inputstream = client.getInputStream()
            val ins = ObjectInputStream(inputstream)
            val fc: FileContainer = ins.readObject() as FileContainer
            print(fc.filename)
            print("===>$fc.filename")
            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */
            val f = File(
                Environment.getExternalStorageDirectory().absolutePath +
                        "/wifip2pshared-${fc.filename}"
            )
            val dirs = File(f.parent)

            dirs.takeIf { it.doesNotExist() }?.apply {
                mkdirs()
            }
            f.createNewFile()
            f.writeBytes(fc.data);
            runBlocking {
                _binding?.textviewStatus?.text = fc.filename.toString()
            }
//            copyFile(inputstream, FileOutputStream(f))
            inputstream.close()
            ins.close()

            serverSocket.close()
            f.absolutePath
        }
    }

    private fun File.doesNotExist(): Boolean = !exists()
    fun copyFile(input: InputStream?, output: OutputStream?): Boolean {
        try {
            val buf = ByteArray(1024)
            var len: Int = 0
            while (input?.read(buf).also {
                    if (it != null) {
                        len = it
                    }
                }!! > 0) {
                output?.write(buf, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                if (input != null) input.close()
                if (output != null) output.close()
            } catch (e: Exception) {
            }
        }
        return true
    }

    fun receive() {
        val context = getContext()
        val host: String
        val port: Int
        var len: Int
        val socket = Socket()
        val buf = ByteArray(1024)

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null)
            socket.connect((InetSocketAddress(InetAddress.getByName("192.168.49.103"), 8888)), 500)

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data is retrieved by the server device.
             */
            val outputStream = socket.getOutputStream()
            val cr = context?.contentResolver
            val inputStream: InputStream =
                cr?.openInputStream(
                    Uri.parse(
                        Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath + "/SherHotel/ss.jpg"))
                            .toString()
                    )
                )!!
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: FileNotFoundException) {
            print(e)
            //catch logic
        } catch (e: IOException) {
            print(e)
        } finally {
            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            socket.takeIf { it.isConnected }?.apply {
                close()
            }
        }
    }

    fun receivepicker(uri: Uri) {
        val context = getContext()
        val host: String
        val port: Int
        var len: Int
        val socket = Socket()
        val buf = ByteArray(1024)

        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null)
            socket.connect((InetSocketAddress(InetAddress.getByName("192.168.49.103"), 8888)), 500)

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data is retrieved by the server device.
             */
            val fileContainer = FileContainer()
//            val contentResolver = context.contentResolver

            val file = FileUtil.from(context,uri);
            fileContainer.filename = file?.name
            fileContainer.size = file?.length();
            fileContainer.data = file?.readBytes()
            val outputStream = socket.getOutputStream()
            val objectOutputStream = ObjectOutputStream(outputStream)
            objectOutputStream.writeObject(fileContainer)

            print("Hell")
//            val cr = context?.contentResolver
//            val inputStream: InputStream =
//                cr?.openInputStream(
//                    uri
//                )!!
//            while (inputStream.read(buf).also { len = it } != -1) {
//                outputStream.write(buf, 0, len)
//            }
            outputStream.close()
            objectOutputStream.close()
//            inputStream.close()
        } catch (e: FileNotFoundException) {
            print(e)
            //catch logic
        } catch (e: IOException) {
            print(e)
        } finally {
            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            socket.takeIf { it.isConnected }?.apply {
                close()
            }
        }
    }
    @SuppressLint("Range")
    fun getImagePath(uri: Uri?): String? {
        var cursor: Cursor? =
            uri?.let { context?.getContentResolver()!!.query(it, null, null, null, null) }
        cursor?.moveToFirst()
        var document_id: String = cursor!!.getString(0)
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
        cursor?.close()
        cursor = context?.getContentResolver()!!.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, MediaStore.Images.Media._ID + " = ? ", arrayOf(document_id), null
        )
        cursor!!.moveToFirst()
        val path: String = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
        return path
    }

    fun getFileFromUri(uri: Uri): File? {
        if (uri.path == null) {
            return null
        }
        var realPath = String()
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        if (uri.path!!.contains("/document/image:")) {
            databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            selection = "_id=?"
            selectionArgs = arrayOf(DocumentsContract.getDocumentId(uri).split(":")[1])
        } else {
            databaseUri = uri
            selection = null
            selectionArgs = null
        }
        try {
            val column = "_data"
            val projection = arrayOf(column)
            val cursor = context?.contentResolver?.query(
                databaseUri,
                projection,
                selection,
                selectionArgs,
                null
            )
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    realPath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.i("GetFileUri Exception:", e.message ?: "")
        }
        val path = if (realPath.isNotEmpty()) realPath else {
            when {
                uri.path!!.contains("/document/raw:") -> uri.path!!.replace(
                    "/document/raw:",
                    ""
                )
                uri.path!!.contains("/document/primary:") -> uri.path!!.replace(
                    "/document/primary:",
                    "/storage/emulated/0/"
                )
                else -> return null
            }
        }
        return File(path)
    }
}