package yokai.domain.base.models

data class Version(
    val type: Type,
    val stage: Stage,
    val major: Int,
    val minor: Int = 0,
    val patch: Int = 0,
    val hotfix: Int = 0,
    val build: Int = 0
) {
    operator fun compareTo(other: Version): Int {
        if (type == Type.DEBUG) {
            throw IllegalStateException("Checking debug version is not allowed")
        }

        if (type != other.type) {
            throw IllegalArgumentException("Can't compare two different version type")
        }

        // On nightly we only care about build number
        if (type == Type.NIGHTLY) return build.compareTo(other.build)

        val currentVer = listOf(major, minor, patch, hotfix, stage.weight, build)
        val otherVer = listOf(other.major, other.minor, other.patch, other.hotfix, other.stage.weight, other.build)

        // In case my brain fried and left out a value
        if (currentVer.size != otherVer.size) throw RuntimeException("Version lists' size must be the same")

        for (i in 1..currentVer.size) {
            when (currentVer[i - 1].compareTo(otherVer[i - 1])) {
                0 -> if (i == currentVer.size) return 0 else continue
                1 -> return 1
                else -> return -1
            }
        }

        return 0
    }

    override fun toString(): String {
        return "$major.$minor.$patch" + (if (hotfix > 0) ".$hotfix" else "") + (if (build > 0) "-${type.prefix}$build" else "")
    }

    companion object {
        fun parse(string: String): Version {
            var type = when {
                string.startsWith("r") -> Type.NIGHTLY
                string.startsWith("d") -> Type.DEBUG
                else -> Type.STABLE
            }

            val split = string.split("-")
            var stage = Stage.RELEASE
            val stageCandidate = split.getOrNull(1)
            if (stageCandidate != null) {
                when {
                    stageCandidate.startsWith("r", true) -> type = Type.NIGHTLY
                    stageCandidate.startsWith("d", true) -> type = Type.DEBUG
                }

                if (type == Type.STABLE)
                    stage = when {
                        stageCandidate.startsWith("b", true) -> Stage.BETA
                        stageCandidate.startsWith("a", true) -> Stage.ALPHA
                        else -> Stage.RELEASE
                    }
            }
            val cleanBuild = stageCandidate
                ?.replace("[^\\d.-]".toRegex(), "")  // remove the prefix
                ?.toInt()
            val candidate = split.first()
                .replace("[^\\d.-]".toRegex(), "")  // remove the prefix
                .split(".").map { it.toInt() }

            if (candidate.size == 1 && type == Type.NIGHTLY)
                return Version(
                    type,
                    stage,
                    0,
                    0,
                    0,
                    0,
                    candidate[0],
                )

            return Version(
                type,
                stage,
                candidate.getOrNull(0) ?: 0,
                candidate.getOrNull(1) ?: 0,
                candidate.getOrNull(2) ?: 0,
                candidate.getOrNull(3) ?: 0,
                cleanBuild ?: 0,
            )
        }
    }

    enum class Stage(val weight: Int) {
        RELEASE(3),
        BETA(2),
        ALPHA(1),
    }

    enum class Type(val prefix: String) {
        STABLE("v"),
        NIGHTLY("r"),
        DEBUG("d"),
    }
}
