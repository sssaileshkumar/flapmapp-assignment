package com.example.cvgl

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

class StreamClient(private val listener: StreamListener? = null) {

    interface StreamListener {
        fun onConnected()
        fun onDisconnected()
        fun onError(t: Throwable)
    }

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected")
                listener?.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "Bytes received: ${bytes.size}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closing: $code / $reason")
                webSocket.close(1000, null)
                listener?.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Error", t)
                listener?.onError(t)
            }
        })
    }

    fun send(data: ByteArray) {
        webSocket?.send(data.toByteString())
    }

    fun close() {
        webSocket?.close(1000, "App closed")
    }

    companion object {
        private const val TAG = "StreamClient"
    }
}
