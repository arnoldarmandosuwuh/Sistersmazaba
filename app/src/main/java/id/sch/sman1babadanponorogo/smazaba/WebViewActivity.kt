package id.sch.sman1babadanponorogo.smazaba

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_web_view.*

const val EXTRA_URL = "extra_url"

class WebViewActivity : AppCompatActivity() {

    companion object {
        private val file_perm = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val url = intent.getStringExtra(EXTRA_URL)

        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.loadUrl(url)

        val webViewSetting = myWebView.settings

        webViewSetting.javaScriptEnabled = true
        webViewSetting.allowFileAccess = true
        webViewSetting.allowFileAccessFromFileURLs = true
        webViewSetting.allowUniversalAccessFromFileURLs = true
        webViewSetting.domStorageEnabled = true
        myWebView.webViewClient = MyWebViewClient()

        myWebView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->

            if (!check_permission(1)) {
                ActivityCompat.requestPermissions(
                    this@WebViewActivity,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    file_perm
                )
            } else {
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimetype)
                request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("Downloading file...")
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimetype)
                )
                val dm = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                dm.enqueue(request)
                Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun check_permission(permission: Int): Boolean {
        when (permission) {
            1 -> return ContextCompat.checkSelfPermission(
                this@WebViewActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            2 -> return ContextCompat.checkSelfPermission(
                this@WebViewActivity,
                Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED
            3 -> return ContextCompat.checkSelfPermission(
                this@WebViewActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, Url: String): Boolean {
            view.loadUrl(Url)
            return true
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
