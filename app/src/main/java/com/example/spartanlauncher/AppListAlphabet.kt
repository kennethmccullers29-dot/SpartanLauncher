package com.example.spartanlauncher

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

internal fun appListSection(label: String): String {
    val trimmed = label.trim()
    if (trimmed.isEmpty()) return "#"
    val first = String(Character.toChars(trimmed.codePointAt(0)))
    val folded = Normalizer.normalize(first, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase(Locale.ROOT)
    if (folded.length == 1 && folded[0] in 'A'..'Z') return folded
    return if (Character.isLetter(trimmed.codePointAt(0))) first.uppercase(Locale.ROOT) else "#"
}

internal fun orderedAppSections(sections: Collection<String>): List<String> {
    val collator = Collator.getInstance(Locale.getDefault())
    return sections.distinct().sortedWith { left, right ->
        when {
            left == right -> 0
            left == "#" -> -1
            right == "#" -> 1
            else -> collator.compare(left, right).takeIf { it != 0 } ?: left.compareTo(right)
        }
    }
}

internal fun appListJumpLetters(available: Set<String>): List<String> =
    listOf("#") + ('A'..'Z').map(Char::toString) +
        orderedAppSections(available.filterNot { it == "#" || (it.length == 1 && it[0] in 'A'..'Z') })

internal fun appListSectionStarts(sectionSizes: List<Pair<String, Int>>): Map<String, Int> {
    var position = 0
    return buildMap {
        sectionSizes.forEach { (section, size) ->
            put(section, position)
            position += size + 1 // Each section includes its letter header.
        }
    }
}

private fun normalizedAppSearch(value: String): String =
    Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase(Locale.ROOT)

internal fun matchesAppSearch(label: String, query: String): Boolean =
    normalizedAppSearch(label).contains(normalizedAppSearch(query))
