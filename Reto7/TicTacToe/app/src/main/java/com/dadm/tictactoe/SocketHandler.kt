import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    lateinit var mSocket: Socket
    var mConnected: Boolean = false

    @Synchronized
    fun setSocket() {
        try {
            if(!mConnected) {
// "http://10.0.2.2:3000" is the network your Android emulator must use to join the localhost network on your computer
// "http://localhost:3000/" will not work
// If you want to use your physical phone you could use your ip address plus :3000
// This will allow your Android Emulator and physical device at your home to connect to the server
                mSocket = IO.socket("http://10.0.2.2:5000")
            }
        } catch (e: URISyntaxException) {
            e.message?.let { Log.e("TEST", it) }
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }


    @Synchronized
    fun establishConnection() {
        if(!mConnected) {
            mSocket.connect()
            mConnected = true
        }
    }

    @Synchronized
    fun closeConnection() {
        mConnected = false
        mSocket.disconnect()
    }
}