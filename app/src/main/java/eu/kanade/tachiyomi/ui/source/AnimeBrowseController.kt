package eu.kanade.tachiyomi.ui.source

import android.view.LayoutInflater
import eu.kanade.tachiyomi.databinding.BrowseControllerBinding
import eu.kanade.tachiyomi.ui.base.controller.BaseController

class AnimeBrowseController : BaseController<BrowseControllerBinding>() {
    override fun createBinding(inflater: LayoutInflater) = BrowseControllerBinding.inflate(inflater)
    
    override fun getTitle(): String? = "Anime Browse"
}
