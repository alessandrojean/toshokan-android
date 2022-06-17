package io.github.alessandrojean.toshokan.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import io.github.alessandrojean.toshokan.util.extension.collectAsStateWithLifecycle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import logcat.logcat

/**
 * Observe the network availability in a Compose way.
 * https://medium.com/scalereal/observing-live-connectivity-status-in-jetpack-compose-way-f849ce8431c7
 */

sealed class ConnectionState {
  object Available : ConnectionState()
  object Unavailable: ConnectionState()
}

val Context.currentConnectivityState: ConnectionState
  get() {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return getCurrentConnectivityState(connectivityManager)
  }

private fun getCurrentConnectivityState(connectivityManager: ConnectivityManager): ConnectionState {
//  val connected = connectivityManager.allNetworks.any { network ->
//    connectivityManager.getNetworkCapabilities(network)
//      ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//      ?: false
//  }
  val activeNetwork = connectivityManager.activeNetwork
    ?: return ConnectionState.Unavailable
  val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    ?: return ConnectionState.Unavailable

  val connected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

  return if (connected) ConnectionState.Available else ConnectionState.Unavailable
}

fun Context.observeConnectivityAsFlow() = callbackFlow {
  val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  val callback = networkCallback { connectionState -> trySend(connectionState) }

  val networkRequest = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .build()

  connectivityManager.registerNetworkCallback(networkRequest, callback)

  val currentState = getCurrentConnectivityState(connectivityManager)
  trySend(currentState)

  awaitClose {
    connectivityManager.unregisterNetworkCallback(callback)
  }
}

fun networkCallback(callback: (ConnectionState) -> Unit): ConnectivityManager.NetworkCallback {
  return object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      callback(ConnectionState.Available)
    }

    override fun onLost(network: Network) {
      callback(ConnectionState.Unavailable)
    }
  }
}

@Composable
fun connectivityState(): State<ConnectionState> {
  val context = LocalContext.current

  return context.observeConnectivityAsFlow().collectAsStateWithLifecycle(
    initialValue = context.currentConnectivityState
  )
}