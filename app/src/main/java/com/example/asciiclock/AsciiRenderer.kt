package com.example.asciiclock

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AsciiRenderer {

    const val STYLE_SHADED = 0
    const val STYLE_MINIMAL = 1

    private val SHADED_DIGITS: Map<Char, Array<String>> = mapOf(
        '0' to arrayOf(
            " .+@@+. ",
            "+@#::#@+",
            "@#:  :#@",
            "@#:  :#@",
            "+@#::#@+",
            " .+@@+. "
        ),
        '1' to arrayOf(
            "  .+@#  ",
            ".+@###  ",
            "  :###  ",
            "  :###  ",
            "  :###  ",
            ".+@@@@+."
        ),
        '2' to arrayOf(
            ".+@@@@+.",
            "@#:  :#@",
            "    .+@:",
            "  .+@+: ",
            ".+@+:   ",
            "+@@@@@@+"
        ),
        '3' to arrayOf(
            "+@@@@@@+",
            "    :+@+",
            "  .+@@+.",
            "    :+@+",
            "@#:  :#@",
            ".+@@@@+."
        ),
        '4' to arrayOf(
            "   .+@@.",
            "  +@#@@.",
            ".+@+:@@.",
            "+@@@@@@+",
            "     @@.",
            "    +@@+"
        ),
        '5' to arrayOf(
            "+@@@@@@+",
            "@@:     ",
            "+@@@@@+.",
            "    :+@+",
            "@#:  :#@",
            ".+@@@@+."
        ),
        '6' to arrayOf(
            " .+@@@+.",
            "+@+:    ",
            "+@@@@@+.",
            "@#:  :#@",
            "@#:  :#@",
            " .+@@@+."
        ),
        '7' to arrayOf(
            "+@@@@@@+",
            "    :+@+",
            "   .+@: ",
            "  .+@:  ",
            "  +@:   ",
            " .@+    "
        ),
        '8' to arrayOf(
            " .+@@@+.",
            "@#:  :#@",
            " .+@@@+.",
            "@#:  :#@",
            "@#:  :#@",
            " .+@@@+."
        ),
        '9' to arrayOf(
            " .+@@@+.",
            "@#:  :#@",
            " .+@@@@+",
            "    :+@+",
            "   .+@: ",
            ".+@@@:  "
        ),
        ':' to arrayOf(
            "   ",
            ".@.",
            ":+:",
            "   ",
            ".@.",
            ":+:"
        )
    )

    private val MINIMAL_DIGITS: Map<Char, Array<String>> = mapOf(
        '0' to arrayOf(
            " :####: ",
            "##:  :##",
            "##    ##",
            "##    ##",
            "##:  :##",
            " :####: "
        ),
        '1' to arrayOf(
            "  :+##  ",
            ":+####  ",
            "  :###  ",
            "  :###  ",
            "  :###  ",
            ":######:"
        ),
        '2' to arrayOf(
            ":######:",
            "##:  :##",
            "    :+##",
            "  :+##: ",
            ":+##:   ",
            "########"
        ),
        '3' to arrayOf(
            "########",
            "    :+##",
            "  :###: ",
            "    :+##",
            "##:  :##",
            ":######:"
        ),
        '4' to arrayOf(
            "   :+## ",
            " :##:## ",
            ":## :## ",
            "########",
            "    :## ",
            "   :###:"
        ),
        '5' to arrayOf(
            "########",
            "##:     ",
            "#######:",
            "    :+##",
            "##:  :##",
            ":######:"
        ),
        '6' to arrayOf(
            " :#####:",
            "+##:    ",
            "#######:",
            "##:  :##",
            "##:  :##",
            " :#####:"
        ),
        '7' to arrayOf(
            "########",
            "    :+##",
            "   :+## ",
            "  :+##  ",
            " :##:   ",
            " ##:    "
        ),
        '8' to arrayOf(
            " :#####:",
            "##:  :##",
            " :#####:",
            "##:  :##",
            "##:  :##",
            " :#####:"
        ),
        '9' to arrayOf(
            " :#####:",
            "##:  :##",
            " :######",
            "    :+##",
            "   :+## ",
            ":#####: "
        ),
        ':' to arrayOf(
            "   ",
            " # ",
            " : ",
            "   ",
            " # ",
            " : "
        )
    )

    fun renderTime(context: Context, style: Int = getSavedStyle(context), now: Date = Date()): String {
        val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm"
        val timeStr = SimpleDateFormat(pattern, Locale.US).format(now)
        val font = if (style == STYLE_MINIMAL) MINIMAL_DIGITS else SHADED_DIGITS

        val rows = Array(6) { StringBuilder() }
        for (i in timeStr.indices) {
            val glyph = font[timeStr[i]] ?: continue
            for (r in 0 until 6) {
                if (i > 0) rows[r].append(' ')
                rows[r].append(glyph[r])
            }
        }
        return rows.joinToString("\n") { it.toString() }
    }

    fun renderDate(now: Date = Date()): String {
        val fmt = SimpleDateFormat("EEE · d MMM", Locale.getDefault())
        return ":: ${fmt.format(now).lowercase(Locale.getDefault())} ::"
    }

    fun getSavedStyle(context: Context): Int {
        return context.getSharedPreferences("ascii_clock_prefs", Context.MODE_PRIVATE)
            .getInt("ascii_style", STYLE_SHADED)
    }

    fun setSavedStyle(context: Context, style: Int) {
        context.getSharedPreferences("ascii_clock_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("ascii_style", style)
            .apply()
    }
}
