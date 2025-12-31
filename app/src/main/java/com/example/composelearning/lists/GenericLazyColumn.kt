package com.example.composelearning.lists

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Composable
fun <T> GenericLazyColumn(
    items: List<T>,
    title: String,
    itemKey: (T) -> Any,
    onItemClick: (T) -> Unit,
    itemSpacing: Dp = 12.dp,
    // This lambda defines what the row looks like for any type T,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(items.size,
            key = { index -> itemKey(items[index])  }) { index ->
            val product = items[index]
            Box(modifier = Modifier.clickable { onItemClick(product) }) {
                itemContent(product)
            }
        }
    }
}

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String
)

// Dummy data generator
val dummyProducts = listOf(
    Product(1, "Wireless Headphones", 99.99, "https://fastly.picsum.photos/id/155/200/200.jpg?hmac=D_Tf9XAIteS9U6InmFX2j3DXYkvhlEOOkGGiWuMwU9Q"),
    Product(2, "Smart Watch", 149.50, "https://fastly.picsum.photos/id/155/200/200.jpg?hmac=D_Tf9XAIteS9U6InmFX2j3DXYkvhlEOOkGGiWuMwU9Q"),
    Product(3, "Bluetooth Speaker", 45.00, "https://fastly.picsum.photos/id/155/200/200.jpg?hmac=D_Tf9XAIteS9U6InmFX2j3DXYkvhlEOOkGGiWuMwU9Q")
)

@Composable
fun ProductListScreen() {
    GenericLazyColumn(
        items = dummyProducts,
        title = "Our Catalog",
        itemKey = { product -> product.id },
        onItemClick = { product -> println("Clicked: ${product.name}") }
    ) { product ->
        // This is the custom UI for a "Product" item
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val context = LocalContext.current
                val imageLoader = initUntrustImageLoader(context)
                AsyncImage(
                    model = product.imageUrl,
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Error -> {
                                // Check Logcat for "CoilError"
                                Log.e("CoilError", "Failed with: ${state.result.throwable.message}")
                            }
                            is AsyncImagePainter.State.Success -> Log.d("CoilSuccess", "Image Loaded")
                            else -> {}
                        }
                    }
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = product.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "$${product.price}", color = Color.White)
                }
            }
        }
    }
}

@SuppressLint("CustomX509TrustManager")
private fun initUntrustImageLoader(context: Context): ImageLoader {
    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

    // Install the all-trusting trust manager
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    // Create an ssl socket factory with our all-trusting manager
    val sslSocketFactory = sslContext.socketFactory

    val client = OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }.build()


    return ImageLoader.Builder(context)
        .okHttpClient(client)
        .build()
}