package eu.kanade.tachiyomi.ui.recents

import android.view.LayoutInflater
import android.view.View
import eu.kanade.tachiyomi.databinding.RecentsControllerBinding
import eu.kanade.tachiyomi.ui.base.controller.BaseCoroutineController

class AnimeRecentsController : BaseCoroutineController<RecentsControllerBinding, AnimeRecentsPresenter>() {
    override var presenter = AnimeRecentsPresenter()

    override fun createBinding(inflater: LayoutInflater) = RecentsControllerBinding.inflate(inflater)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        // TODO: Port RecentsController logic for Anime
    }
    
    override fun getTitle(): String? = "Anime Recents"
}
