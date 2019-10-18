package id.sch.sman1babadanponorogo.smazaba

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_web_view.*
import java.util.*

const val EXTRA_URL = "extra_url"

class WebViewActivity : AppCompatActivity() {

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILECHOOSER_RESULTCODE = 1
    lateinit var myWebView: WebView
    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var downloadListener: DownloadListener
    private lateinit var mRandom:Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    var writeAccess = false

    /** Permission Request Code */
    private val PERMISSION_REQUEST_CODE = 1234

    var siteUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled", "OverridingDeprecatedMember")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        context = applicationContext
        activity = this

        myWebView = findViewById(R.id.webview)

        siteUrl = intent.getStringExtra(EXTRA_URL)

        // Initialize a new Random instance
        mRandom = Random()

        // Initialize the handler instance
        mHandler = Handler()

        // Check permission to write in external storage
        checkWriteAccess()

        // Create a Download Listener
        createDownloadListener()

        // Display Toast Message When Download Complete
        onDownloadComplete()

        // Configure Web View
        configureWebView(siteUrl)

        // Swipe refresh
        swipeRefresh.setOnRefreshListener {
            mRunnable = Runnable {
                myWebView.reload()
                swipeRefresh.isRefreshing = false
            }
            // Execute the task after specified time
            mHandler.postDelayed(
                mRunnable,
                (randomInRange(1, 5) * 1000).toLong() // Delay 1 to 5 seconds
            )
        }


    }

    // Custom method to get a random number from the provided range
    private fun randomInRange(min:Int, max:Int):Int{
        // Define a new Random class
        val r = Random()

        // Get the next random number within range
        // Including both minimum and maximum number
        return r.nextInt((max - min) + 1) + min;
    }

    @Suppress("DEPRECATION")
    private fun createDownloadListener() {
        downloadListener =
            DownloadListener { url, userAgent, contentDescription, mimetype, contentLength ->
                val request = DownloadManager.Request(Uri.parse(url))
                val cookies = CookieManager.getInstance().getCookie(url)

                request.addRequestHeader("cookie", cookies)
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("Sistersmazaba")
                request.setMimeType(mimetype)
                request.setTitle(URLUtil.guessFileName(url, contentDescription, mimetype))
                request.setAllowedOverMetered(true)
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                val fileName = URLUtil.guessFileName(url, contentDescription, mimetype)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                val dManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                if (writeAccess)
                    dManager.enqueue(request)
                else {
                    Toast.makeText(
                        context,
                        "Unable to download file. Required Privileges are not available.",
                        Toast.LENGTH_LONG
                    ).show()
                    checkWriteAccess()
                }

            }
    }

    private fun checkWriteAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage("Required permission to write external storage to save downloaded file.")
                    builder.setTitle("Please Grant Write Permission")
                    builder.setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(WRITE_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                    builder.setNeutralButton("Cancel", null)
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                writeAccess = true
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(url: String?) {
        if (checkConnection(context)) {
            myWebView.settings.javaScriptEnabled = true
            myWebView.loadUrl(url)
            myWebView.setDownloadListener(downloadListener)
            myWebView.webViewClient = MyWebViewClient()
            myWebView.webChromeClient = MyWebChromeClient()

        } else {
            noConnection()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILECHOOSER_RESULTCODE) {
            mFilePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    resultCode,
                    data
                )
            )
            mFilePathCallback = null
        }
    }

    private fun noConnection() {
        webview.visibility = View.GONE
        msw_progress.visibility = View.GONE
        txtNoConnection.visibility = View.VISIBLE
        ivNoConnection.visibility = View.VISIBLE
        btnTry.visibility = View.VISIBLE
        btnTry.setOnClickListener {
            finish()
            startActivity(intent)
        }
    }

    private fun onDownloadComplete() {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                Toast.makeText(context, "File Downloaded", Toast.LENGTH_LONG).show()
            }
        }

        /** Register to receives above broadcast */
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeAccess = true
                    // Do the task now
                    toast("Permission granted.")
                } else {
                    writeAccess = false
                    toast("Permission denied.")
                }
            }
        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            msw_progress.progress = newProgress
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            mFilePathCallback = filePathCallback
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, "Image Browser"),
                FILECHOOSER_RESULTCODE
            )
            return true
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            msw_progress.visibility = View.VISIBLE
            webview.visibility = View.GONE
            txtNoConnection.visibility = View.GONE
            ivNoConnection.visibility = View.GONE
            btnTry.visibility = View.GONE
        }

        override fun shouldOverrideUrlLoading(view: WebView, Url: String): Boolean {
            if (checkConnection(context)) {
                if (Url.contains("sman1babadanponorogo.sch.id")) {
                    view.loadUrl(Url)
                    return false
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Url))
                    startActivity(intent)
                    return true
                }
            } else {
                noConnection()
            }
            return true

        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            msw_progress.visibility = View.GONE
            webview.visibility = View.VISIBLE
        }

    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            if (checkConnection(context)) {
                webview.goBack()
            } else {
                noConnection()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            if (checkConnection(context)) {
                webview.goBack()
            } else {
                noConnection()
            }
            return true
        } else {
            return super.onKeyDown(keyCode, event)

        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ServiceCast")
    fun checkConnection(context: Context): Boolean {
        var available = false
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivity.allNetworkInfo
        for (i in networkInfo.indices) {
            if (networkInfo[i].state == NetworkInfo.State.CONNECTED) {
                available = true
                break
            }
        }
        return available
    }

    // Extension function to show toast message
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

