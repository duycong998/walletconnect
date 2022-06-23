package jp.co.awl.aicamera.walletconnect.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import jp.co.awl.aicamera.walletconnect.ExampleApplication.Companion.session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.walletconnect.Session
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

internal class WalletRepository(val context: Context) : WalletManager {

    override fun openWallet() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("wc:")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override suspend fun performTransaction(
        to: String,
        address: String,
        value: String,
        data: String?,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
    ): Session.MethodCall.Response {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                val id = System.currentTimeMillis()
                session.performMethodCall(
                    Session.MethodCall.SendTransaction(
                        id,
                        address,
                        to,
                        nonce,
                        gasPrice,
                        gasLimit,
                        value,
                        data ?: ""
                    )
                ) { response -> onResponse(id, response, continuation) }
                openWallet()
            }
        }
    }


    override suspend fun performTransaction(
        to: String,
        address: String,
        value: String,
        nonce: String?,
        gasPrice: String?,
        gasLimit: String?,
    ) = performTransaction(to,address, value, null, nonce, gasLimit, gasLimit)


    private fun onResponse(
        id: Long,
        response: Session.MethodCall.Response,
        continuation: Continuation<Session.MethodCall.Response>
    ) {
        if (id != response.id) {
            val throwable = Throwable("The response id is different from the transaction id!")
            continuation.resumeWith(Result.failure(throwable))
            return
        }
        response.error?.let {
            continuation.resumeWith(Result.failure(Throwable(it.message)))
        } ?: continuation.resumeWith(Result.success(response))
    }
}