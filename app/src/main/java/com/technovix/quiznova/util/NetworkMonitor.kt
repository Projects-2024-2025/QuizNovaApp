package com.technovix.quiznova.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(@ApplicationContext private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        // Başlangıç durumunu hemen gönder
        trySend(isCurrentlyConnected())

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch { send(true) } // Coroutine scope içinde gönder
            }

            override fun onLost(network: Network) {
                launch { send(false) } // Coroutine scope içinde gönder
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                // Bazen onAvailable/onLost tetiklenmeyebilir, yetenek değişikliği ile kontrol et
                launch { send(hasInternetCapability(networkCapabilities)) }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // İnternet erişimini dinle
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Flow kapatıldığında callback'i kaldır
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Sadece durum değiştiğinde yeni değer gönder

    private fun isCurrentlyConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null && hasInternetCapability(connectivityManager.getNetworkCapabilities(activeNetwork))
    }

    private fun hasInternetCapability(caps: NetworkCapabilities?): Boolean {
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true // Gerçek internet erişimi var mı kontrolü
    }
}