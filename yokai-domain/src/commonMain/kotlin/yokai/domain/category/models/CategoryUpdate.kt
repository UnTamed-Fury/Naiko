package yokai.domain.category.models

data class CategoryUpdate(
    val id: Long,
    val name: String? = null,
    val mangaOrder: String? = null,
    val order: Long? = null,
    val flags: Long? = null,
)
