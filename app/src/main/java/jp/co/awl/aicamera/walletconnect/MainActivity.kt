package jp.co.awl.aicamera.walletconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import jp.co.awl.aicamera.walletconnect.component.WalletRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow

class MainActivity : AppCompatActivity(), Session.Callback {
    private var txRequest: Long? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val walletRepository by lazy { WalletRepository(this) }
    override fun onStatus(status: Session.Status) {
        when (status) {
            Session.Status.Approved -> {
                sessionApproved()
                Log.d("###", "Approved")
            }
            Session.Status.Closed -> {
                sessionClosed()
                Log.d("###", "Closed")
            }
            Session.Status.Connected -> Log.d("###", "Connected")
            Session.Status.Disconnected -> Log.d("###", "Disconnected")
            is Session.Status.Error -> {
                // Do Stuff
                Log.d("###", "Error")
            }
        }
    }

    override fun onMethodCall(call: Session.MethodCall) {
        if(call.id() == txRequest) {
            Log.d("####onMethodCall", "CCCCC")
        }

    }

    private fun sessionApproved() {
        uiScope.launch {
            screen_main_status.text = "Connected: ${ExampleApplication.session.approvedAccounts()}"
            screen_main_connect_button.visibility = View.GONE
            screen_main_disconnect_button.visibility = View.VISIBLE
            screen_main_tx_button.visibility = View.VISIBLE
        }
    }

    private fun sessionClosed() {
        uiScope.launch {
            screen_main_status.text = "Disconnected"
            screen_main_connect_button.visibility = View.VISIBLE
            screen_main_disconnect_button.visibility = View.GONE
            screen_main_tx_button.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        initialSetup()
        screen_main_connect_button.setOnClickListener {
            ExampleApplication.resetSession()
            ExampleApplication.session.addCallback(this)
            try {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(ExampleApplication.config.toWCUri())
                })
            }catch (e: Exception) {
                Log.d("AAAA", e.toString() + "no ví")
            }
        }
        screen_main_disconnect_button.setOnClickListener {
            ExampleApplication.session.kill()
        }
        screen_main_tx_button.setOnClickListener {
            val from = "0xa226A802f1A7b2434A4210A059914f08993CcF0e"
            val txRequest = System.currentTimeMillis()
            Log.d("###time", txRequest.toString())
            lifecycleScope.launch {
                runCatching {
                    ExampleApplication.session.performMethodCall(
                        Session.MethodCall.SendTransaction(
                            txRequest,
                            from,
                            "0x24EdA4f7d0c466cc60302b9b5e9275544E5ba552",
                            null,
                            null,
                            null,
                            "0.002",
                            ""
                        ),
                        ::handleResponse
                    )
                }.onFailure { Log.d("#####onFailure", it.message.toString())  }
                    .onSuccess {
                        Log.d("#####onSuccess", "onSuccess")
                        openWallet()}
            }
            this.txRequest = txRequest
            Log.d("###txRequest", txRequest.toString())
            Log.d("AAA", "ok")
        }
    }

    //    screen_main_tx_button.setOnClickListener {
//        val from = "0xa226A802f1A7b2434A4210A059914f08993CcF0e"
//        val txRequest = System.currentTimeMillis()
//        Log.d("###time", txRequest.toString())
//        lifecycleScope.launch {
//            runCatching {
//                walletRepository.performTransaction(
//                    from,
//                    "0x4D3C4aDCdc3993D3d1DB5bAAfc463d38E2995c1B",
//                    "0.02",
//                    null,
//                    null,
//                    "")
//            }.onFailure { Log.d("#####onFailure", it.message.toString())  }
//                .onSuccess { Log.d("#####onSuccess", "onSuccess")  }
//        }
//        openWallet()
//        this.txRequest = txRequest
//        Log.d("###txRequest", txRequest.toString())
//        Log.d("AAA", "ok")
//    }

    fun openWallet() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("wc:")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun initialSetup() {
        val session = nullOnThrow { ExampleApplication.session } ?: return
        session.addCallback(this)
        sessionApproved()
    }

    private fun handleResponse(resp: Session.MethodCall.Response) {
        Log.d("AAAid",resp.id .toString()  )
        Log.d("AAAtxRequest", txRequest.toString())
        if (resp.id == txRequest) {
            txRequest = null
            uiScope.launch {
                screen_main_response.visibility = View.VISIBLE
                screen_main_response.text =
                    "Last response: " + ((resp.result as? String) ?: "Unknown response")
            }
        }
    }

    override fun onDestroy() {
        ExampleApplication.session.removeCallback(this)
        super.onDestroy()
    }
}
