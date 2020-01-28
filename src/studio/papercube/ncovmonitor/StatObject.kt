package studio.papercube.ncovmonitor

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_ABSENT)
open class StatObject private constructor(_name: String? = null) {
    var name: String? = _name
    val value = HashMap<Any?, Any?>()

    companion object {
        @JvmStatic
        fun of(name: String? = null, vararg children: Pair<*, *>) = StatObject(name).apply {
            value.putAll(children)
        }
    }
}