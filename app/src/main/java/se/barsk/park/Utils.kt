package se.barsk.park

object Utils {
    /**
     * Adds a http:// to the beginning of an url if it doesn't have any protocol
     * Adds a / to the end of an url if it doesn't have any
     */
    fun fixUrl(url: String): String {
        if (url.isEmpty()) {
            return ""
        }

        var fixedUrl = url
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            fixedUrl = "http://" + fixedUrl
        }
        if (!url.endsWith("/")) {
            fixedUrl += "/"
        }

        return fixedUrl
    }
}