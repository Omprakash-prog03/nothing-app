package com.pokecharge

import android.app.Application

/**
 * No Hilt / DI framework is used (brief §12: "use Hilt only if it improves
 * maintainability" - for this app's small, mostly-singleton dependency graph
 * it would add build complexity without a real benefit). Repositories are
 * plain lazily-initialised singletons (see their `getInstance(context)`
 * companions) constructed from `applicationContext`, which this class exists
 * mainly to document.
 */
class PokeChargeApplication : Application()
