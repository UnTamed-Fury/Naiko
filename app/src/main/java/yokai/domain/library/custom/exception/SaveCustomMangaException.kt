package yokai.domain.library.custom.exception

import java.io.IOException

/**
 * Exception to abstract over SQLiteException and SQLiteConstraintException for multiplatform.
 *
 * @param throwable the source throwable to include for tracing.
 */
class SaveCustomMangaException(throwable: Throwable) : IOException("Error Saving Custom Manga Info to Database", throwable)
