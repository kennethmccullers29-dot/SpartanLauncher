package com.ken1dot.spartanlauncher

import java.text.Collator
import java.text.Normalizer
import java.util.Locale
import android.icu.text.AlphabeticIndex
import android.icu.util.ULocale

private val pinyinLocale = ULocale("zh@collation=pinyin")

// ICU pinyin index: buckets Han characters (Simplified and Traditional) under Latin A-Z.
private val pinyinIndex by lazy {
    runCatching { AlphabeticIndex<Any>(pinyinLocale).buildImmutableIndex() }.getOrNull()
}

private fun isHan(codePoint: Int): Boolean =
    Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN

private fun pinyinSection(text: String): String? {
    val index = pinyinIndex ?: return null
    val label = index.getBucket(index.getBucketIndex(text))?.label ?: return null
    return label.uppercase(Locale.ROOT).takeIf { it.length == 1 && it[0] in 'A'..'Z' }
}

/** Sort order for All apps: pinyin-aware so Chinese labels sit under their romanized letter. */
internal fun appListCollator(): Comparator<String> {
    val icu = android.icu.text.Collator.getInstance(pinyinLocale)
    val local = Collator.getInstance(Locale.getDefault())
    return Comparator { l, r ->
        val a = l.trim(); val b = r.trim()
        val han = (a.isNotEmpty() && isHan(a.codePointAt(0))) || (b.isNotEmpty() && isHan(b.codePointAt(0)))
        if (han) icu.compare(a, b) else local.compare(a, b)
    }
}

internal fun appListSection(label: String): String {
    val trimmed = label.trim()
    if (trimmed.isEmpty()) return "#"
    val first = String(Character.toChars(trimmed.codePointAt(0)))
    val folded = Normalizer.normalize(first, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase(Locale.ROOT)
    if (folded.length == 1 && folded[0] in 'A'..'Z') return folded
    if (isHan(trimmed.codePointAt(0))) return pinyinSection(trimmed) ?: "#"
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
