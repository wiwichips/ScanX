package com.dynamsoft.sample.dbrcamerapreview

import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatDelegate
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup

/**
 * A [android.preference.PreferenceActivity] which implements and proxies the necessary calls
 * to be used with AppCompat.
 */
abstract class AppCompatPreferenceActivity : PreferenceActivity() {

    private lateinit var mDelegate: AppCompatDelegate
    private var inited = false
    private val delegate: AppCompatDelegate
        get() {
            if (!inited) {
                mDelegate = AppCompatDelegate.create(this, null)
                inited = true
            }
            return mDelegate
        }

    override fun onCreate(savedInstanceState: Bundle) {
        delegate.installViewFactory()
        delegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle) {
        super.onPostCreate(savedInstanceState)
        delegate.onPostCreate(savedInstanceState)
    }

    override fun getMenuInflater(): MenuInflater {
        return delegate.menuInflater
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        delegate.setContentView(layoutResID)
    }

    override fun setContentView(view: View) {
        delegate.setContentView(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate.setContentView(view, params)
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        delegate.addContentView(view, params)
    }

    override fun onPostResume() {
        super.onPostResume()
        delegate.onPostResume()
    }

    override fun onTitleChanged(title: CharSequence, color: Int) {
        super.onTitleChanged(title, color)
        delegate.setTitle(title)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        delegate.onConfigurationChanged(newConfig)
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }

    override fun invalidateOptionsMenu() {
        delegate.invalidateOptionsMenu()
    }
}