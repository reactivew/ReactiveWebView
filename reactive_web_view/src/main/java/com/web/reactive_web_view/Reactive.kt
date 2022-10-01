package com.web.reactive_web_view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Message
import android.util.Log
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher

@SuppressLint("SetJavaScriptEnabled")
fun WebView.makeReactive(
    context: Context,
    launcher: ActivityResultLauncher<String>,
    call: (ValueCallback<Array<Uri?>>) -> Unit,
    nextUrl: (String) -> Unit
) {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.loadWithOverviewMode = false
    settings.userAgentString = settings.userAgentString.replace("wv", "")

    CookieManager.getInstance().setAcceptCookie(true)
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

    webViewClient = object : WebViewClient() {
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            Log.e("Reactive", error.description.toString())
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            CookieManager.getInstance().flush()
            nextUrl(url)
        }
    }

    webChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri?>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            call(filePathCallback)
            launcher.launch("image/*")
            return true
        }

        @SuppressLint("SetJavaScriptEnabled")
        override fun onCreateWindow(
            view: WebView?, isDialog: Boolean,
            isUserGesture: Boolean, resultMsg: Message
        ): Boolean {
            val newWebView = WebView(context)
            newWebView.settings.javaScriptEnabled = true
            newWebView.webChromeClient = this
            newWebView.settings.javaScriptCanOpenWindowsAutomatically = true
            newWebView.settings.domStorageEnabled = true
            newWebView.settings.setSupportMultipleWindows(true)
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = newWebView
            resultMsg.sendToTarget()
            return true
        }
    }
}