package studio.papercube.ncovmonitor

class BadResponseException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(throwable: Throwable) : super(throwable)
}