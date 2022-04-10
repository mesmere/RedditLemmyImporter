import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.File
import java.time.ZonedDateTime

data class Post(
    @JsonDeserialize(using = Parser.DeletedToNullDeserializer::class)
    val author: String?,
    val created_utc: ZonedDateTime,
    val is_self: Boolean,
    val name: String,
    val score: Int,
    val selftext: String,
    val title: String,
    val url: String,
)

data class Comment(
    @JsonDeserialize(using = Parser.DeletedToNullDeserializer::class)
    val author: String?,
    val body: String,
    val created_utc: ZonedDateTime,
    val name: String,
    val parent_id: String,
    val score: Int,
)

fun main(args: Array<String>) {
    File("genZhouArchive.sql").bufferedWriter().use { output ->
        {}.javaClass.getResource("/GenZhouArchive.json").openStream().reader().useLines { lines ->
            for (line in lines) {
                val (post, comments) = Parser(line)
                Writer(post, comments, output)
            }
        }
    }
}