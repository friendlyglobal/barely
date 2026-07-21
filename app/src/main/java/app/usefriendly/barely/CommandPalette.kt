package app.usefriendly.barely

import android.content.Context
import android.provider.Settings
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

data class LauncherContact(
    val name: String,
    val phoneNumber: String,
)

enum class CommandIcon {
    CALCULATE,
    CONVERT,
    SETTINGS,
    CONTACT,
    ASSISTANT,
    NOTIFICATIONS,
    MEDIA,
}

sealed interface LauncherCommandAction {
    data class CopyResult(val value: String) : LauncherCommandAction
    data class OpenSettings(val intentAction: String) : LauncherCommandAction
    data class Dial(val phoneNumber: String) : LauncherCommandAction
    data class AskAssistant(
        val packageName: String,
        val prompt: String,
    ) : LauncherCommandAction
    data object RequestContactsPermission : LauncherCommandAction
    data object OpenNotificationAccess : LauncherCommandAction
    data class ToggleNotificationDots(val enabled: Boolean) : LauncherCommandAction
    data class ToggleMediaControls(val enabled: Boolean) : LauncherCommandAction
}

data class LauncherCommand(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: CommandIcon,
    val score: Int,
    val action: LauncherCommandAction,
)

internal fun buildLauncherCommands(
    context: Context,
    query: String,
    installedPackages: Set<String>,
    contacts: List<LauncherContact>,
    hasContactsPermission: Boolean,
    hasNotificationAccess: Boolean,
    notificationDotsEnabled: Boolean,
    mediaControlsEnabled: Boolean,
): List<LauncherCommand> {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return emptyList()
    val normalizedQuery = trimmedQuery.normalizedForSearch()

    return buildList {
        calculate(trimmedQuery)?.let { calculation ->
            add(
                LauncherCommand(
                    key = "calculate:${calculation.expression}",
                    title = calculation.result,
                    subtitle = context.getString(
                        R.string.command_calculation_subtitle,
                        calculation.expression,
                    ),
                    icon = CommandIcon.CALCULATE,
                    score = -20,
                    action = LauncherCommandAction.CopyResult(calculation.result),
                ),
            )
        }
        convert(trimmedQuery)?.let { conversion ->
            add(
                LauncherCommand(
                    key = "convert:${conversion.source}:${conversion.target}",
                    title = conversion.result,
                    subtitle = context.getString(
                        R.string.command_conversion_subtitle,
                        conversion.source,
                        conversion.target,
                    ),
                    icon = CommandIcon.CONVERT,
                    score = -18,
                    action = LauncherCommandAction.CopyResult(conversion.result),
                ),
            )
        }

        QUICK_ACTIONS.forEach { quickAction ->
            relevanceScore(
                normalizedQuery,
                quickAction.keywords.map(::SearchTerm),
            )?.let { score ->
                add(
                    LauncherCommand(
                        key = "setting:${quickAction.intentAction}",
                        title = context.getString(quickAction.title),
                        subtitle = context.getString(R.string.command_quick_setting_subtitle),
                        icon = CommandIcon.SETTINGS,
                        score = score + 4,
                        action = LauncherCommandAction.OpenSettings(quickAction.intentAction),
                    ),
                )
            }
        }

        relevanceScore(
            normalizedQuery,
            NOTIFICATION_KEYWORDS.map(::SearchTerm),
        )?.let { score ->
            if (!hasNotificationAccess) {
                add(
                    LauncherCommand(
                        key = "notifications:access",
                        title = context.getString(R.string.command_enable_notification_access),
                        subtitle = context.getString(R.string.command_notification_access_warning),
                        icon = CommandIcon.NOTIFICATIONS,
                        score = score,
                        action = LauncherCommandAction.OpenNotificationAccess,
                    ),
                )
            } else {
                add(
                    LauncherCommand(
                        key = "notifications:dots",
                        title = context.getString(
                            if (notificationDotsEnabled) {
                                R.string.command_disable_notification_dots
                            } else {
                                R.string.command_enable_notification_dots
                            },
                        ),
                        subtitle = context.getString(R.string.command_notification_dots_subtitle),
                        icon = CommandIcon.NOTIFICATIONS,
                        score = score,
                        action = LauncherCommandAction.ToggleNotificationDots(
                            !notificationDotsEnabled,
                        ),
                    ),
                )
                add(
                    LauncherCommand(
                        key = "notifications:media",
                        title = context.getString(
                            if (mediaControlsEnabled) {
                                R.string.command_disable_media_controls
                            } else {
                                R.string.command_enable_media_controls
                            },
                        ),
                        subtitle = context.getString(R.string.command_media_controls_subtitle),
                        icon = CommandIcon.MEDIA,
                        score = score + 1,
                        action = LauncherCommandAction.ToggleMediaControls(!mediaControlsEnabled),
                    ),
                )
                add(
                    LauncherCommand(
                        key = "notifications:manage",
                        title = context.getString(R.string.command_manage_notification_access),
                        subtitle = context.getString(R.string.command_notification_access_warning),
                        icon = CommandIcon.SETTINGS,
                        score = score + 8,
                        action = LauncherCommandAction.OpenNotificationAccess,
                    ),
                )
            }
        }

        assistantPrompt(trimmedQuery)?.takeIf { it.isNotBlank() }?.let { prompt ->
            ASSISTANTS.filter { it.packageName in installedPackages }.forEach { assistant ->
                add(
                    LauncherCommand(
                        key = "assistant:${assistant.packageName}",
                        title = context.getString(R.string.command_ask_assistant, assistant.name),
                        subtitle = prompt,
                        icon = CommandIcon.ASSISTANT,
                        score = -16 + assistant.rank,
                        action = LauncherCommandAction.AskAssistant(
                            packageName = assistant.packageName,
                            prompt = prompt,
                        ),
                    ),
                )
            }
        }

        if (hasContactsPermission) {
            contacts.forEach { contact ->
                relevanceScore(
                    normalizedQuery,
                    listOf(
                        SearchTerm(contact.name),
                        SearchTerm(contact.phoneNumber, penalty = 20, allowFuzzy = false),
                    ),
                )?.let { score ->
                    add(
                        LauncherCommand(
                            key = "contact:${contact.phoneNumber}:${contact.name}",
                            title = contact.name,
                            subtitle = contact.phoneNumber,
                            icon = CommandIcon.CONTACT,
                            score = score + 3,
                            action = LauncherCommandAction.Dial(contact.phoneNumber),
                        ),
                    )
                }
            }
        } else if (CONTACT_KEYWORDS.any { keyword ->
                keyword.normalizedForSearch().startsWith(normalizedQuery) ||
                    normalizedQuery.startsWith(keyword.normalizedForSearch())
            }
        ) {
            add(
                LauncherCommand(
                    key = "contacts:permission",
                    title = context.getString(R.string.command_enable_contacts),
                    subtitle = context.getString(R.string.command_contacts_private),
                    icon = CommandIcon.CONTACT,
                    score = 5,
                    action = LauncherCommandAction.RequestContactsPermission,
                ),
            )
        }
    }.distinctBy(LauncherCommand::key).sortedBy(LauncherCommand::score)
}

internal data class Calculation(
    val expression: String,
    val result: String,
)

internal fun calculate(rawQuery: String): Calculation? {
    var expression = rawQuery.trim()
    val prefixes = listOf("calculate ", "calculator ", "calcular ", "calc ", "=")
    prefixes.firstOrNull { expression.lowercase(Locale.ROOT).startsWith(it) }?.let { prefix ->
        expression = expression.drop(prefix.length).trim()
    }
    expression = expression
        .replace('×', '*')
        .replace('÷', '/')
        .replace(',', '.')
    if (expression.length !in 3..80 || expression.none { it in "+-*/%^" }) return null
    if (!expression.matches("[0-9.eE+\\-*/%^() ]+".toRegex())) return null

    val result = runCatching { ExpressionParser(expression).parse() }.getOrNull()
        ?.takeIf(Double::isFinite)
        ?: return null
    return Calculation(
        expression = expression,
        result = formatNumber(result),
    )
}

internal data class Conversion(
    val source: String,
    val target: String,
    val result: String,
)

internal fun convert(rawQuery: String): Conversion? {
    val match = CONVERSION_PATTERN.matchEntire(rawQuery.trim().lowercase(Locale.ROOT)) ?: return null
    val value = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
    val sourceToken = match.groupValues[2]
    val targetToken = match.groupValues[3]
    val source = UNITS[sourceToken] ?: return null
    val target = UNITS[targetToken] ?: return null
    if (source.dimension != target.dimension || sourceToken == targetToken) return null

    val converted = if (source.dimension == Dimension.TEMPERATURE) {
        convertTemperature(value, source.symbol, target.symbol)
    } else {
        value * source.toBase / target.toBase
    }
    if (!converted.isFinite()) return null
    return Conversion(
        source = "${formatNumber(value)} ${source.display}",
        target = target.display,
        result = "${formatNumber(converted)} ${target.display}",
    )
}

private class ExpressionParser(private val source: String) {
    private var index = 0

    fun parse(): Double {
        val value = expression()
        skipSpaces()
        require(index == source.length) { "Unexpected input" }
        return value
    }

    private fun expression(): Double {
        var value = term()
        while (true) {
            value = when {
                consume('+') -> value + term()
                consume('-') -> value - term()
                else -> return value
            }
        }
    }

    private fun term(): Double {
        var value = power()
        while (true) {
            value = when {
                consume('*') -> value * power()
                consume('/') -> value / power()
                consume('%') -> value % power()
                else -> return value
            }
        }
    }

    private fun power(): Double {
        val value = unary()
        return if (consume('^')) value.pow(power()) else value
    }

    private fun unary(): Double = when {
        consume('+') -> unary()
        consume('-') -> -unary()
        consume('(') -> expression().also { require(consume(')')) { "Missing parenthesis" } }
        else -> number()
    }

    private fun number(): Double {
        skipSpaces()
        val start = index
        while (index < source.length && (source[index].isDigit() || source[index] in ".eE")) {
            index++
            if (index < source.length && source[index - 1] in "eE" && source[index] in "+-") index++
        }
        require(index > start) { "Expected number" }
        return source.substring(start, index).toDouble()
    }

    private fun consume(character: Char): Boolean {
        skipSpaces()
        if (source.getOrNull(index) != character) return false
        index++
        return true
    }

    private fun skipSpaces() {
        while (source.getOrNull(index)?.isWhitespace() == true) index++
    }
}

private enum class Dimension { LENGTH, MASS, DATA, TEMPERATURE }

private data class UnitDefinition(
    val dimension: Dimension,
    val symbol: String,
    val display: String = symbol,
    val toBase: Double = 1.0,
)

private fun units(): Map<String, UnitDefinition> = buildMap {
    fun add(definition: UnitDefinition, vararg aliases: String) {
        aliases.forEach { put(it, definition) }
    }

    add(UnitDefinition(Dimension.LENGTH, "mm", toBase = 0.001), "mm", "millimeter", "millimetre")
    add(UnitDefinition(Dimension.LENGTH, "cm", toBase = 0.01), "cm", "centimeter", "centimetre")
    add(UnitDefinition(Dimension.LENGTH, "m"), "m", "meter", "metre", "metro")
    add(UnitDefinition(Dimension.LENGTH, "km", toBase = 1_000.0), "km", "kilometer", "kilometre")
    add(UnitDefinition(Dimension.LENGTH, "in", toBase = 0.0254), "in", "inch", "inches", "polegada")
    add(UnitDefinition(Dimension.LENGTH, "ft", toBase = 0.3048), "ft", "foot", "feet", "pe")
    add(UnitDefinition(Dimension.LENGTH, "yd", toBase = 0.9144), "yd", "yard", "yards")
    add(UnitDefinition(Dimension.LENGTH, "mi", toBase = 1_609.344), "mi", "mile", "miles", "milha")

    add(UnitDefinition(Dimension.MASS, "mg", toBase = 0.000001), "mg", "milligram")
    add(UnitDefinition(Dimension.MASS, "g", toBase = 0.001), "g", "gram", "grama")
    add(UnitDefinition(Dimension.MASS, "kg"), "kg", "kilogram", "quilo")
    add(UnitDefinition(Dimension.MASS, "oz", toBase = 0.028349523125), "oz", "ounce", "ounces")
    add(UnitDefinition(Dimension.MASS, "lb", toBase = 0.45359237), "lb", "lbs", "pound", "pounds", "libra")

    add(UnitDefinition(Dimension.DATA, "B"), "b", "byte", "bytes")
    add(UnitDefinition(Dimension.DATA, "KB", toBase = 1_000.0), "kb")
    add(UnitDefinition(Dimension.DATA, "MB", toBase = 1_000_000.0), "mb")
    add(UnitDefinition(Dimension.DATA, "GB", toBase = 1_000_000_000.0), "gb")
    add(UnitDefinition(Dimension.DATA, "KiB", toBase = 1_024.0), "kib")
    add(UnitDefinition(Dimension.DATA, "MiB", toBase = 1_048_576.0), "mib")
    add(UnitDefinition(Dimension.DATA, "GiB", toBase = 1_073_741_824.0), "gib")

    add(UnitDefinition(Dimension.TEMPERATURE, "c", "°C"), "c", "°c", "celsius")
    add(UnitDefinition(Dimension.TEMPERATURE, "f", "°F"), "f", "°f", "fahrenheit")
    add(UnitDefinition(Dimension.TEMPERATURE, "k", "K"), "k", "kelvin")
}

private fun convertTemperature(value: Double, source: String, target: String): Double {
    val celsius = when (source) {
        "c" -> value
        "f" -> (value - 32.0) * 5.0 / 9.0
        "k" -> value - 273.15
        else -> error("Unknown temperature")
    }
    return when (target) {
        "c" -> celsius
        "f" -> celsius * 9.0 / 5.0 + 32.0
        "k" -> celsius + 273.15
        else -> error("Unknown temperature")
    }
}

private fun assistantPrompt(query: String): String? {
    val lowered = query.lowercase(Locale.ROOT)
    val prefix = ASSISTANT_PREFIXES.firstOrNull(lowered::startsWith) ?: return null
    return query.drop(prefix.length).trim()
}

private fun formatNumber(value: Double): String = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 10
    isGroupingUsed = false
}.format(value)

private data class QuickAction(
    val title: Int,
    val intentAction: String,
    val keywords: List<String>,
)

private data class Assistant(
    val name: String,
    val packageName: String,
    val rank: Int,
)

private val UNITS = units()
private val CONVERSION_PATTERN = Regex(
    "^([+-]?[0-9]+(?:[.,][0-9]+)?)\\s*([°a-z]+)\\s+(?:(?:to|in|em|para|a|en|vers|zu)\\s+)?([°a-z]+)$",
)
private val CONTACT_KEYWORDS = listOf("contact", "contacts", "contato", "contatos", "person", "people")
private val NOTIFICATION_KEYWORDS = listOf(
    "notification", "notifications", "notificacao", "notificacoes", "media", "music", "musica",
)
private val ASSISTANT_PREFIXES = listOf("ask ", "pergunte ", "pergunta ", "ai ", "ia ", "?")
private val ASSISTANTS = listOf(
    Assistant("ChatGPT", "com.openai.chatgpt", 0),
    Assistant("Gemini", "com.google.android.apps.bard", 1),
    Assistant("Claude", "com.anthropic.claude", 2),
)
private val QUICK_ACTIONS = listOf(
    QuickAction(
        R.string.command_internet,
        Settings.Panel.ACTION_INTERNET_CONNECTIVITY,
        listOf("internet", "wifi", "wi-fi", "network", "rede"),
    ),
    QuickAction(
        R.string.command_bluetooth,
        Settings.ACTION_BLUETOOTH_SETTINGS,
        listOf("bluetooth", "bt"),
    ),
    QuickAction(
        R.string.command_display,
        Settings.ACTION_DISPLAY_SETTINGS,
        listOf("display", "screen", "brightness", "tela", "brilho"),
    ),
    QuickAction(
        R.string.command_sound,
        Settings.ACTION_SOUND_SETTINGS,
        listOf("sound", "volume", "audio", "som"),
    ),
    QuickAction(
        R.string.command_battery,
        Settings.ACTION_BATTERY_SAVER_SETTINGS,
        listOf("battery", "power", "bateria", "energia"),
    ),
    QuickAction(
        R.string.command_accessibility,
        Settings.ACTION_ACCESSIBILITY_SETTINGS,
        listOf("accessibility", "acessibilidade"),
    ),
)
