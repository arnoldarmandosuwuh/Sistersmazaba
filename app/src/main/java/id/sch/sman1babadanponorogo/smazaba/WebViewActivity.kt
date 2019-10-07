package id.sch.sman1babadanponorogo.smazaba

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
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

    lateinit var siteUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        siteUrl = intent.getStringExtra(EXTRA_URL)

        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.loadUrl(siteUrl)

        val webViewSetting = myWebView.settings

        webViewSetting.setAppCacheEnabled(true)
        webViewSetting.cacheMode = WebSettings.LOAD_DEFAULT
        webViewSetting.setAppCachePath(cacheDir.path)
        webViewSetting.javaScriptEnabled = true
        webViewSetting.allowFileAccess = true
        webViewSetting.allowFileAccessFromFileURLs = true
        webViewSetting.allowUniversalAccessFromFileURLs = true
        webViewSetting.domStorageEnabled = true
        myWebView.webViewClient = MyWebViewClient()

        myWebView.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                msw_progress.progress = newProgress
            }
        }

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
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalFilesDir(this@WebViewActivity,
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimetype)
                )
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            msw_progress.visibility = View.VISIBLE
            webview.visibility = View.GONE
        }

        override fun shouldOverrideUrlLoading(view: WebView, Url: String): Boolean {
            if(Url.contains("sman1babadanponorogo.sch.id")){
                view.loadUrl(Url)
                return true
            }
            else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Url))
                startActivity(intent)
                return true
            }

        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            msw_progress.visibility = View.GONE
            webview.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()){
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}