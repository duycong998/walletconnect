package jp.co.awl.aicamera.walletconnect.component
import org.walletconnect.Session
interface WalletManager {
    fun openWallet()


    suspend fun performTransaction(
        to: String,
        address: String,
        value: String,
        data: String?,
        nonce: String? = null,
        gasPrice: String? = null,
        gasLimit: String? = null,
    ): Session.MethodCall.Response

    suspend fun performTransaction(
        to: String,
        address: String,
        value: String,
        nonce: String? = null,
        gasPrice: String? = null,
        gasLimit: String? = null,
    ): Session.MethodCall.Response
}
