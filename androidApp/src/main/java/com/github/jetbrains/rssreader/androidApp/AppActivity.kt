package com.github.jetbrains.rssreader.androidApp

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.github.jetbrains.rssreader.androidApp.ui.fragment.BaseFragment
import com.github.jetbrains.rssreader.androidApp.ui.util.doOnApplyWindowInsets
import com.github.terrakok.modo.Modo
import com.github.terrakok.modo.android.ModoRender
import com.github.terrakok.modo.android.init
import com.github.terrakok.modo.back
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class AppActivity : AppCompatActivity(R.layout.container) {
    private val modoRender by lazy { ModoRender(this, R.id.container) }
    private val modo: Modo by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppTheme()
        super.onCreate(savedInstanceState)
        modo.init(savedInstanceState, Screens.MainFeed())
        handleLeftAndRightInsets()
    }

    private fun applyAppTheme() {
        setTheme(R.style.Theme_MyApp)
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                adjustAlpha(context.getColorFromAttr(R.attr.colorSurface), .7f).let { statusColor ->
                    statusBarColor = statusColor
                    navigationBarColor = statusColor
                }
                if (!context.isNightMode()) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                adjustAlpha(
                    context.getColorFromAttr(R.attr.colorOnSurface),
                    .7f
                ).let { statusColor ->
                    statusBarColor = statusColor
                    navigationBarColor = statusColor
                }
            }
        }
    }

    private fun handleLeftAndRightInsets() {
        findViewById<View>(R.id.container).doOnApplyWindowInsets { view, insets, initialPadding ->
            view.updatePadding(
                left = initialPadding.left + insets.systemWindowInsetLeft,
                right = initialPadding.right + insets.systemWindowInsetRight
            )
            WindowInsetsCompat.Builder(insets).setSystemWindowInsets(
                Insets.of(
                    0,
                    insets.systemWindowInsetTop,
                    0,
                    insets.systemWindowInsetBottom
                )
            ).build()
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        modo.render = modoRender
    }

    override fun onPause() {
        modo.render = null
        super.onPause()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.container)
        (currentFragment as? BaseFragment)?.onBackPressed() ?: modo.back()
    }

    @ColorInt
    private fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    @ColorInt
    private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red: Int = Color.red(color)
        val green: Int = Color.green(color)
        val blue: Int = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun Context.isNightMode(): Boolean =
        resources.configuration.uiMode
            .and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

}
