package com.pokecharge.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val COLLECTION = "collection?type={type}"
    const val TYPES = "types"
    const val DETAIL = "detail/{dexId}"
    const val PREVIEW = "preview/{dexId}"
    const val SETTINGS = "settings"

    fun collection(type: String? = null) = if (type != null) "collection?type=$type" else "collection"
    fun detail(dexId: Int) = "detail/$dexId"
    fun preview(dexId: Int) = "preview/$dexId"
}
