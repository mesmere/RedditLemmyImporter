package rileynull

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.apache.commons.text.StringEscapeUtils

object DumpParser {
    class HTMLEntityDecodingConverter : StdConverter<String, String>() {
        override fun convert(str: String): String {
            return StringEscapeUtils.unescapeHtml4(str)
        }
    }

    class URLFixingConverter : StdConverter<String, String>() {
        override fun convert(str: String): String {
            return if (str.startsWith("/r/")) "https://www.reddit.com$str" else str
        }
    }

    private val mapper = jsonMapper {
        addModule(kotlinModule())
        addModule(JavaTimeModule())
        defaultTimeZone(java.util.TimeZone.getTimeZone("UTC"))
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private fun accumulateComments(curNode: JsonNode, acc: MutableList<Comment> = mutableListOf()): List<Comment> {
        acc += mapper.convertValue<Comment>(curNode)
        if (curNode["replies"].isObject) {
            for (child in curNode["replies"]["data"]["children"]) {
                if (child["kind"].asText() == "t1") {
                    accumulateComments(child["data"], acc)
                }
            }
        }
        return acc
    }

    /**
     * Parse a line of JSON from the input (i.e. a single post and all of its associated reply comments).
     */
    operator fun invoke(line: String): Pair<Post, List<Comment>> {
        val json = mapper.readTree(line)
        val post = mapper.convertValue<Post>(json["json"][0]["data"]["children"][0]["data"])
        val comments = json["json"][1]["data"]["children"].flatMap { accumulateComments(it["data"]) }

        // Lemmy will crash if we insert a malformed URL, so we want to crash here first.
        if (!post.is_self) java.net.URL(post.url)

        return Pair(post, comments)
    }
}