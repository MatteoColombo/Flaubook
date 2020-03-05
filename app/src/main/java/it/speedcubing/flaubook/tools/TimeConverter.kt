package it.speedcubing.flaubook.tools


fun timeToString(time: Int): String {
    if (time < 0) return "0s"
    val timeSeconds = time / 1000
    val seconds = timeSeconds % 60
    val minutes = timeSeconds / 60 % 60
    val hours = timeSeconds / 3600
    if (hours > 0) {
        var res = "${hours}h "
        res += if (minutes < 10) "0${minutes}m" else "${minutes}m"
        return res
    } else if (minutes > 0) {
        var res = "${minutes}m "
        res += if (seconds < 10) "0${seconds}s" else "${seconds}s"
        return res
    }
    return "${seconds}s"

}

fun timeToStringShort(time: Int): String {
    if (time < 0) return "0:00"
    val timeSeconds = time / 1000
    val seconds = timeSeconds % 60
    val minutes = timeSeconds / 60
    var res = "${minutes}:"
    res += if (seconds < 10) "0${seconds}" else "$seconds"
    return res
}