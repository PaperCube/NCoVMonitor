package studio.papercube.ncovmonitor.dataconversion

import com.fasterxml.jackson.databind.ObjectMapper
import studio.papercube.ncovmonitor.StatObject
import studio.papercube.ncovmonitor.StatisticItem
import java.io.BufferedReader
import java.io.File
import java.time.LocalDateTime
import java.util.stream.Collectors

object Converter {
    private val Long.versionCategory: Int
        get() {
            val v = toLocalDateTime()
            return when {
                v < LocalDateTime.of(2020, 1, 24, 16, 1) -> 0
                v < LocalDateTime.of(2020, 1, 25, 3, 15) -> 1
                v < LocalDateTime.of(2020, 1, 26, 15, 0) -> 2
                else -> -1
            }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("line.separator", "\n")
        val map = File(".").listFiles()!!
                .toList()
                .stream()
                .collect(Collectors.groupingBy { file: File -> file.lastModified().versionCategory })
        val converters: Map<Int, (BufferedReader) -> Array<Any?>> = mapOf(
                0 to Converter::convertV1,
                1 to Converter::convertV2,
                2 to Converter::convertV3
        )

        val outDir = File("out")
        outDir.mkdirs()
        val valueMapper = ObjectMapper().writerWithDefaultPrettyPrinter()

        for ((key, fileList) in map) {
            val converter = converters[key]
            if (converter != null) {
                fileList.filter { it.length() > 0 }
                        .forEach fileLoop@{ f ->
                            val obj = converter(f.bufferedReader()) // obj: array of a Json object
                            if (obj.isEmpty()) {
                                println("Skipping ${f.name}: empty result")
                                return@fileLoop
                            }
                            val outFile = File(outDir, f.name)
                            valueMapper.writeValue(outFile, obj)
//                            valueMapper.writeValueAsString(obj)
                        }
            } else {
                println("Skipping ${fileList.size} files whose converters are missing")
            }
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun BufferedReader.skipUntilSeparator() {
        while (!readLine().contains("======="));
    }

    private fun BufferedReader.getTextUntilSeparator(): String {
        val sb = StringBuilder()
        var ln: String
        while (!readLine().apply { ln = this }.contains("======")) {
            sb.appendln(ln)
        }
        return sb.toString().trim()
    }

    private fun parseStatisticItemDataClass(str: String): StatisticItem {
        // StatisticItem(confirmedCount=639, suspectedCount=422, curedCount=30, deathCount=17)
        val regex = "StatisticItem\\(confirmedCount=([\\d-]+), suspectedCount=([\\d-]+), curedCount=([\\d-]+), deathCount=([\\d-]+)\\)".toRegex()
        val matches = regex.find(str)!!.groupValues
        return StatisticItem(
                matches[1].toInt(),
                matches[2].toInt(),
                matches[3].toInt(),
                matches[4].toInt()
        )
    }

    private fun convertV1(reader: BufferedReader): Array<Any?> {
        val str = reader.readLine() ?: return emptyArray()
        return arrayOf(
                StatObject.of("dxy",
                        "counts" to parseStatisticItemDataClass(str)
                )
        )
    }

    private fun convertV2(reader: BufferedReader): Array<Any?> = with(reader) {
        readLine()
        val time = object {
            val modifyTime = readLine().toLong()
            val literalTime = readLine()
        }
        val counts = parseStatisticItemDataClass(readLine())
        val provinceData = readLine()
        return arrayOf(
                StatObject.of("dxy",
                        "counts" to counts,
                        "time" to time,
                        "provinceData" to provinceData
                )
        )
    }

    private fun convertV3(reader: BufferedReader): Array<Any?> = with(reader) {
        readLine()
        val dxy = convertV2(reader)[0]!!
        skipUntilSeparator()
        readLine()
        val literalTime = readLine()
        val counts = parseStatisticItemDataClass(readLine())
        val provinceData = getTextUntilSeparator()

        readLine() //Source:___
        readLine() //--
        readLine() //-1,-1,-1,-1

        val sb = StringBuilder()
        repeat(4) { sb.appendln(readLine()) }

        return arrayOf(
                dxy,
                StatObject.of("Netease",
                        "counts" to counts,
                        "time" to literalTime,
                        "provinceData" to provinceData),
                StatObject.of("NeteaseWrappedDxySource",
                        "provinceData" to sb.toString())
        )
    }

}