package org.springsource.restbucks.myapplication.tasks

import android.os.AsyncTask
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springsource.restbucks.myapplication.HypermediaRemoteResource

class CancelOrder(private val self : Link) : AsyncTask<String, String, Any?>() {

    private val client = OkHttpClient()

    override fun doInBackground(vararg params: String?) : Any? {

        val request = Request.Builder().delete()
            .url(self.href)
            .addHeader("Accept", MediaTypes.HAL_JSON_VALUE)
            .build()

        client.newCall(request).execute()

        return null
    }
}