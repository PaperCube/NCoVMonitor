package studio.papercube.ncovmonitor

class BadResponseException : RuntimeException {
    companion object {
        fun noMatch() = BadResponseException("Cannot find result: no match against given pattern found")
        fun nullValue(name:String) = BadResponseException("\$$name is null")
        fun nullBody() = BadResponseException("Successfully retrieved response but got null string")
        fun badHtmlBody() = BadResponseException("Unable to parse html body: value not initialized")
    }

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(throwable: Throwable) : super(throwable)
}