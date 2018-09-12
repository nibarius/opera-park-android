package se.barsk.park

object Utils {
    /**
     * Adds a http:// to the beginning of an url if it doesn't have any protocol
     * Adds a / to the end of an url if it doesn't have any
     * Also trims any leading / trailing whitespace
     */
    fun fixUrl(url: String): String {
        if (url.isEmpty()) {
            return ""
        }

        var fixedUrl = url.trim()
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            fixedUrl = "http://$fixedUrl"
        }
        if (!url.endsWith("/")) {
            fixedUrl += "/"
        }

        return fixedUrl
    }

    /**
     * Takes a licence plate string and makes it consistent and nicer for display.
     * It makes all letters uppercase and tries to insert a space between letters
     * and numbers.
     */
    fun fixRegnoForDisplay(regNo: String): String =
            regNo.toUpperCase().replace("[A-Z][0-9]".toRegex()) { "${it.value[0]} ${it.value[1]}" }
}