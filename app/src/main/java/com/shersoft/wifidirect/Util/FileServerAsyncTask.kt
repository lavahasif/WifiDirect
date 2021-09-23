package com.shersoft.wifidirect.Util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.widget.TextView
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket

class FileServerAsyncTask(
    private val context: Context,
    private var statusText: TextView
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        /**
         * Create a server socket.
         */
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

            val pathname = Environment.getExternalStorageDirectory().absolutePath +
                    "/s.jpg"
print(pathname)
            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */
            val cr = context?.contentResolver
            val f = File(
                pathname
            )
//            val dirs = File(f.parent)

//            dirs.takeIf { it.doesNotExist() }?.apply {
//                mkdirs()
//            }
//            f.createNewFile()



            val inputstream = client.getInputStream()
            runBlocking {
                copyFile(inputstream, FileOutputStream(f))
            }

            serverSocket.close()
            f.absolutePath
        }
    }

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

    private fun File.doesNotExist(): Boolean = !exists()

    /**
     * Start activity that can handle the JPEG image
     */
    override fun onPostExecute(result: String?) {
        result?.run {
            statusText.text = "File copied - $result"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse("file://$result"), "image/*")
            }
            context.startActivity(intent)
        }
    }
}