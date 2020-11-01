package me.zeroeightsix.kami.util.text

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.module.modules.chat.ChatEncryption
import java.util.regex.Pattern

object MessageDetectionHelper {
    fun String.detectAndRemove(regex: Regexes): String? {
        if (!this.detect(regex)) return null

        val removed = regex.regex.replace(this, "")
        return if (removed.isEmpty()) null else {
            removed
        }
    }

    fun String.detect(vararg regexes: Regexes): Boolean {
        regexes.forEach {
            if (it.regex.containsMatchIn(this)) return true
        }
        return false
    }

    fun String.detect(setting: Boolean, vararg regex: Regexes) = setting && this.detect(*regex)

    fun getMessageType(direct: Boolean, message: String, server: String): String {
        if (message.detect(direct, Regexes.DIRECT) || message.detect(direct, Regexes.DIRECT_ALT)) return "You got a direct message!\n"
        if (message.detect(direct, Regexes.DIRECT_SENT)) return "You sent a direct message!\n"
        if (message == "KamiBlueMessageType1") return "Connected to $server"
        return if (message == "KamiBlueMessageType2") "Disconnected from $server" else ""
    }

    fun isDirect(direct: Boolean, message: String) = message.detect(direct, Regexes.DIRECT, Regexes.DIRECT_ALT, Regexes.DIRECT_SENT)

    fun getDirectUsername(message: String): String? {
        if (!isDirect(true, message)) return null

        /* side note: this won't work if some glitched account has spaces in their username, but in all honesty, like 3 people globally have those */
        var username = message.split(" ")[0]
        if (username.detect(Regexes.DIRECT_ALT) && username.length > 1) {
            username = username.substring(1)
        }

        return username
    }

    fun shouldSend(all: Boolean, restart: Boolean, direct: Boolean, queue: Boolean, importantPings: Boolean, message: String): Boolean {
        return all
                || message.detect(restart, Regexes.RESTART)
                || isDirect(direct, message)
                || message.detect(queue, Regexes.QUEUE)
                || message.detect(importantPings, Regexes.QUEUE_IMPORTANT)

    }

    fun isCommand(string: String) = commandPrefixes.firstOrNull { string.startsWith(it) } != null

    fun isKamiCommand(string: String) = string.startsWith(Command.getCommandPrefix())

    fun String.find(regex: String): Boolean = Pattern.compile(regex).matcher(this).find()

    private val commandPrefixes: Array<String>
        get() = arrayOf("/", ",", ".", "-", ";", "?", "*", "^", "&", "%", "#", "$",
                Command.getCommandPrefix(),
                ChatEncryption.delimiterValue.value)
}

enum class Regexes(val regex: Regex) {
    DIRECT(Regex("^([0-9A-z_])+ whispers( to you|): ")),
    DIRECT_ALT(Regex("^\\[.*->.*] ")),
    DIRECT_SENT(Regex("^to ([0-9A-z_])+: ")),
    QUEUE(Regex("^Position in queue: ")),
    QUEUE_IMPORTANT(Regex("^Position in queue: [1-5]$")),
    RESTART(Regex("^\\[SERVER] Server restarting in ")),
    TPA_REQUEST(Regex("^([0-9A-z_])+ (has requested|wants) to teleport to you\\."))
}
