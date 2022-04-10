import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.File
import java.time.ZonedDateTime

data class Post(
    @JsonDeserialize(converter = Parser.DeletedToNullConverter::class)
    val author: String?,
    val created_utc: ZonedDateTime,
    val is_self: Boolean,
    val name: String,
    val score: Int,
    @JsonDeserialize(converter = Parser.HTMLEntityDecode::class)
    val selftext: String,
    val title: String,
    val url: String,
)

data class Comment(
    @JsonDeserialize(converter = Parser.DeletedToNullConverter::class)
    val author: String?,
    @JsonDeserialize(converter = Parser.HTMLEntityDecode::class)
    val body: String,
    val created_utc: ZonedDateTime,
    val name: String,
    val parent_id: String,
    val score: Int,
)

fun main(args: Array<String>) {
    val writer = Writer(
        targetCommName = args.getOrElse(0) { "genzhouarchive" },
        targetUserName = args.getOrElse(1) { "archive_bot" }
    )

    println("Running...")
    File("GenZhouArchive.sql").bufferedWriter(Charsets.UTF_8).use { output ->
        {}.javaClass.getResource("/GenZhouArchive.json").openStream().reader().useLines { lines ->
            for (line in lines) {
                val (post, comments) = Parser(line)
                writer(post, comments, output)
            }
        }
    }
    println("Done!")
}