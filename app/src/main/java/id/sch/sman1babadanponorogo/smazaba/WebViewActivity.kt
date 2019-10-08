package id.sch.sman1babadanponorogo.smazaba

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.sch.sman1babadanponorogo.smazaba.util.ManagePermissions
import kotlinx.android.synthetic.main.activity_web_view.*

const val EXTRA_URL = "extra_url"

class WebViewActivity : AppCompatActivity() {

    private val PermissionRequestCode = 123
    private lateinit var managePermissions: ManagePermissions
    var siteUrl: String? = null
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var filePath: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled", "OverridingDeprecatedMember")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val list = listOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        managePermissions = ManagePermissions(this,list,PermissionRequestCode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            managePermissions.checkPermissions()
        }
        siteUrl = intent.getStringExtra(EXTRA_URL)

        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.loadUrl(siteUrl)

        CookieManager.getInstance().acceptCookie()
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

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                msw_progress.progress = newProgress
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if(filePath != null){
                    filePath!!.onReceiveValue(null)
                }

                filePath = filePathCallback

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("*/*")
                val PICKFILE_REQUEST_CODE = 100
                startActivityForResult(intent, PICKFILE_REQUEST_CODE)
                return false
            }
        }

        myWebView.setDownloadListener (DownloadListener{ url, userAgent, contentDescription, mimetype, contentLength ->



                val request = DownloadManager.Request(Uri.parse(url))
                val cookies = CookieManager.getInstance().getCookie(url)

                request.addRequestHeader("cookie", cookies)
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("Sistersmazaba")
                request.setMimeType(mimetype)
//                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                val fileName = URLUtil.guessFileName(url, contentDescription, mimetype)

                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                request.setTitle(URLUtil.guessFileName(url, contentDescription, mimetype))

                request.setAllowedOverMetered(true)
                request.setAllowedOverRoaming(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    request.setRequiresCharging(false)
                    request.setRequiresDeviceIdle(false)
                }
//                request.setVisibleInDownloadsUi(true)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PermissionRequestCode ->{
                val isPermissionsGranted = managePermissions.processPermissionsResult(requestCode, permissions, grantResults)
                if(isPermissionsGranted){
                    // Do the task now
                    toast("Permission(s) granted.")
                }else{
                    toast("Permission(s) denied.")
                }
                return
            }
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            msw_progress.visibility = View.VISIBLE
            webview.visibility = View.GONE
        }

        override fun shouldOverrideUrlLoading(view: WebView, Url: String): Boolean {
            if (Url.contains("sman1babadanponorogo.sch.id")) {
                view.loadUrl(Url)
                return true
            } else {
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
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Extension function to show toast message
    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}