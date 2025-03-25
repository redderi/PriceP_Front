package com.redderi.pricep.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

fun ChangeLocale(context: Context, languageCode: String) {
    val configuration = Configuration(context.resources.configuration)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.setLocale(Locale(languageCode))
    } else {
        configuration.locale = Locale(languageCode)
    }

    context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
}