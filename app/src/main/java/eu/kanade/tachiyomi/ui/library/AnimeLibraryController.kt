package eu.kanade.tachiyomi.ui.library

import android.view.LayoutInflater
import eu.kanade.tachiyomi.databinding.LibraryControllerBinding
import eu.kanade.tachiyomi.ui.base.controller.BaseController

class AnimeLibraryController : BaseController<LibraryControllerBinding>() {
    override fun createBinding(inflater: LayoutInflater) = LibraryControllerBinding.inflate(inflater)
    
    override fun getTitle(): String? = "Anime Library"
}
