package studio.papercube.ncovmonitor

internal fun String.trimToIntOrNull(): Int? {
    return trim().toIntOrNull()
}

internal fun Regex.extractGroupIn(str: String, pos: Int): String? {
    return find(str, 0)?.groupValues?.getOrNull(pos)
}

internal fun String.extractIntFromRegex(pos: Int, regex: Regex): Int? {
    val str = regex.extractGroupIn(this, pos)
    val ret = str?.trimToIntOrNull()
    if (ret == null) {
        log.w("$this: Match against $regex failed")
    }
    return ret
}