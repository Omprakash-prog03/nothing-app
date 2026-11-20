package com.pokecharge.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

/** Pushes an immediate widget refresh - see [PokeChargeWidgetProvider] doc for why. */
object WidgetUpdater {
    fun requestUpdate(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, PokeChargeWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            val provider = PokeChargeWidgetProvider()
            provider.onUpdate(context, manager, ids)
        }
    }
}
