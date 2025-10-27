package yokai.core.metadata

import eu.kanade.tachiyomi.data.database.models.Chapter
import eu.kanade.tachiyomi.source.model.SManga
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

const val COMIC_INFO_EDITS_FILE = "ComicInfoEdits.xml"
const val COMIC_INFO_FILE = "ComicInfo.xml"

fun getComicInfo(
    manga: SManga,
    chapter: Chapter,
    urls: List<String>,
    categories: List<String>?,
    sourceName: String,
    lang: String?,
) = ComicInfo(
    title = ComicInfo.Title(chapter.name.stripNonValidXML1_0Characters()),
    series = ComicInfo.Series(manga.title.stripNonValidXML1_0Characters()),
    number = chapter.chapter_number.takeIf { it >= 0 }?.let {
        if (it.rem(1) == 0.0f) {
            ComicInfo.Number(it.toInt().toString())
        } else {
            ComicInfo.Number(it.toString())
        }
    },
    summary = manga.description?.let { ComicInfo.Summary(it.stripNonValidXML1_0Characters()) },
    writer = manga.author?.let { ComicInfo.Writer(it.stripNonValidXML1_0Characters()) },
    penciller = manga.artist?.let { ComicInfo.Penciller(it.stripNonValidXML1_0Characters()) },
    inker = null,
    colorist = null,
    letterer = null,
    coverArtist = null,
    translator = chapter.scanlator?.let { ComicInfo.Translator(it.stripNonValidXML1_0Characters()) },
    genre = manga.genre?.let { ComicInfo.Genre(it.stripNonValidXML1_0Characters()) },
    tags = null,
    web = ComicInfo.Web(urls.joinToString(" ")),
    publishingStatus = ComicInfo.PublishingStatusTachiyomi(
        ComicInfoPublishingStatus.toComicInfoValue(manga.status.toLong())
    ),
    categories = categories?.let { ComicInfo.CategoriesTachiyomi(it.joinToString()) },
    source = ComicInfo.SourceMihon(sourceName),
    language = lang?.let { ComicInfo.LanguageJ2K(it) },
)

fun SManga.toComicInfo(lang: String? = null) = ComicInfo(
    title = null,
    series = ComicInfo.Series(title.stripNonValidXML1_0Characters()),
    number = null,
    summary = description?.let { ComicInfo.Summary(it.stripNonValidXML1_0Characters()) },
    writer = author?.let { ComicInfo.Writer(it.stripNonValidXML1_0Characters()) },
    penciller = artist?.let { ComicInfo.Penciller(it.stripNonValidXML1_0Characters()) },
    inker = null,
    colorist = null,
    letterer = null,
    coverArtist = null,
    translator = null,
    genre = genre?.let { ComicInfo.Genre(it.stripNonValidXML1_0Characters()) },
    tags = null,
    web = null,
    publishingStatus = ComicInfo.PublishingStatusTachiyomi(
        ComicInfoPublishingStatus.toComicInfoValue(status.toLong())
    ),
    categories = null,
    source = null,
    language = lang?.let { ComicInfo.LanguageJ2K(it) },
)

fun SManga.copyFromComicInfo(comicInfo: ComicInfo) {
    comicInfo.series?.let { title = it.value }
    comicInfo.writer?.let { author = it.value }
    comicInfo.summary?.let { description = it.value }

    listOfNotNull(
        comicInfo.genre?.value,
        comicInfo.tags?.value,
        comicInfo.categories?.value,
    )
        .distinct()
        .joinToString(", ") { it.trim() }
        .takeIf { it.isNotEmpty() }
        ?.let { genre = it }

    listOfNotNull(
        comicInfo.penciller?.value,
        comicInfo.inker?.value,
        comicInfo.colorist?.value,
        comicInfo.letterer?.value,
        comicInfo.coverArtist?.value,
    )
        .flatMap { it.split(", ") }
        .distinct()
        .joinToString(", ") { it.trim() }
        .takeIf { it.isNotEmpty() }
        ?.let { artist = it }

    status = ComicInfoPublishingStatus.toSMangaValue(comicInfo.publishingStatus?.value)
}

// TODO: Move to core module
// REF: https://anansi-project.github.io/docs/comicinfo/schemas/v2.0
@Serializable
@XmlSerialName("ComicInfo", "", "")
data class ComicInfo(
    val title: Title?,
    val series: Series?,
    val number: Number?,
    val summary: Summary?,
    val writer: Writer?,
    val penciller: Penciller?,
    val inker: Inker?,
    val colorist: Colorist?,
    val letterer: Letterer?,
    val coverArtist: CoverArtist?,
    val translator: Translator?,
    val genre: Genre?,
    val tags: Tags?,
    val web: Web?,
    val publishingStatus: PublishingStatusTachiyomi?,
    val categories: CategoriesTachiyomi?,
    val source: SourceMihon?,
    val language: LanguageJ2K?,
) {
    @XmlElement(false)
    @XmlSerialName("xmlns:xsd", "", "")
    val xmlSchema: String = "http://www.w3.org/2001/XMLSchema"

    @XmlElement(false)
    @XmlSerialName("xmlns:xsi", "", "")
    val xmlSchemaInstance: String = "http://www.w3.org/2001/XMLSchema-instance"

    @Serializable
    @XmlSerialName("Title", "", "")
    data class Title(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Series", "", "")
    data class Series(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Number", "", "")
    data class Number(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Summary", "", "")
    data class Summary(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Writer", "", "")
    data class Writer(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Penciller", "", "")
    data class Penciller(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Inker", "", "")
    data class Inker(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Colorist", "", "")
    data class Colorist(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Letterer", "", "")
    data class Letterer(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("CoverArtist", "", "")
    data class CoverArtist(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Translator", "", "")
    data class Translator(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Genre", "", "")
    data class Genre(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Tags", "", "")
    data class Tags(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Web", "", "")
    data class Web(@XmlValue(true) val value: String = "")

    // Tachi Note: The spec doesn't have a good field for this
    // REF: https://github.com/anansi-project/comicinfo/issues/5
    @Serializable
    @XmlSerialName("PublishingStatusTachiyomi", "http://www.w3.org/2001/XMLSchema", "ty")
    data class PublishingStatusTachiyomi(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("Categories", "http://www.w3.org/2001/XMLSchema", "ty")
    data class CategoriesTachiyomi(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("SourceMihon", "http://www.w3.org/2001/XMLSchema", "mh")
    data class SourceMihon(@XmlValue(true) val value: String = "")

    @Serializable
    @XmlSerialName("LanguageJ2K", "http://www.w3.org/2001/XMLSchema", "j2k")
    data class LanguageJ2K(@XmlValue(true) val value: String = "")
}

enum class ComicInfoPublishingStatus(
    val comicInfoValue: String,
    val sMangaModelValue: Int,
) {
    ONGOING("Ongoing", SManga.ONGOING),
    COMPLETED("Completed", SManga.COMPLETED),
    LICENSED("Licensed", SManga.LICENSED),
    PUBLISHING_FINISHED("Publishing finished", SManga.PUBLISHING_FINISHED),
    CANCELLED("Cancelled", SManga.CANCELLED),
    ON_HIATUS("On hiatus", SManga.ON_HIATUS),
    UNKNOWN("Unknown", SManga.UNKNOWN),
    ;

    companion object {
        fun toComicInfoValue(value: Long): String {
            return entries.firstOrNull { it.sMangaModelValue == value.toInt() }?.comicInfoValue
                ?: UNKNOWN.comicInfoValue
        }

        fun toSMangaValue(value: String?): Int {
            return entries.firstOrNull { it.comicInfoValue == value }?.sMangaModelValue
                ?: UNKNOWN.sMangaModelValue
        }
    }
}

// REF: https://www.w3.org/TR/xml/#charsets
fun String.stripNonValidXML1_0Characters() = filter { it.isValidXML1_0() }

fun Char.isValidXML1_0() = code.let { c ->
    c == 0x9 ||
    c == 0xA ||
    c == 0xD ||
    c in 0x20..0xD7FF ||
    c in 0xE000..0xFFFD ||
    c in 0x10000..0x10FFFF
}
