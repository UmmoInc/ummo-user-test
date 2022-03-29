package xyz.ummo.user.data.repo

import xyz.ummo.user.data.db.AllServicesDatabase
//TODO: Saved for [UMMO-75]
/** This repo will get data from DB and/or our API and propagate it to the viewModel associated **/
class AllServicesRepository(
    val db: AllServicesDatabase
)