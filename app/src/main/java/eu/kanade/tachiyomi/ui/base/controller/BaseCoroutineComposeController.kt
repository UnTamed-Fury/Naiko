package eu.kanade.tachiyomi.ui.base.controller

import android.os.Bundle
import eu.kanade.tachiyomi.ui.base.presenter.BaseCoroutinePresenter

abstract class BaseCoroutineComposeController<PS : BaseCoroutinePresenter<*>>(bundle: Bundle? = null) :
    BaseComposeController(bundle) {

    abstract val presenter: PS
}
