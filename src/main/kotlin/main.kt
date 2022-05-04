package rileynull

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.time.ZonedDateTime
import kotlin.system.exitProcess

data class Post(
    val author: String,
    val created_utc: ZonedDateTime,
    val is_self: Boolean,
    val name: String,
    val score: Int,
    @JsonDeserialize(converter = DumpParser.HTMLEntityDecodingConverter::class)
    val selftext: String,
    val subreddit_name_prefixed: String,
    val title: String,
    @JsonDeserialize(converter = DumpParser.URLFixingConverter::class)
    val url: String,
)

data class Comment(
    val author: String,
    @JsonDeserialize(converter = DumpParser.HTMLEntityDecodingConverter::class)
    val body: String,
    val created_utc: ZonedDateTime,
    val name: String,
    val parent_id: String,
    val score: Int,
    val subreddit_name_prefixed: String,
)

@Command(name = "RedditLemmyImporter", versionProvider = App.ManifestVersionProvider::class, abbreviateSynopsis = true,
    mixinStandardHelpOptions = true, showDefaultValues = true, sortOptions = false, usageHelpAutoWidth = true)
class App : Runnable {
    @Option(names = ["-c", "--comm"], paramLabel = "name", description = ["Target community name."], required = true)
    lateinit var commName: String

    @Option(names = ["-u", "--user"], paramLabel = "name", description = ["Target user name."], required = true)
    lateinit var userName: String

    @Option(names = ["-o", "--output-file"], paramLabel = "file", converter = [DashFileTypeConverter::class],
        description = ["Output SQL file.", "  Prints to stdout if this option isn't specified."])
    var outfile: File? = null

    @Parameters(index = "0", paramLabel = "dump_file", converter = [DashFileTypeConverter::class],
        description = ["Path to the JSON dump from the Reddit API.", "  Specify - to read from stdin."])
    var infile: File? = null

    class ManifestVersionProvider : CommandLine.IVersionProvider {
        override fun getVersion(): Array<String> {
            // Pull the app version out of the manifest that Maven builds into the jar.
            return arrayOf(App::class.java.`package`.implementationVersion ?: "unknown")
        }
    }

    class DashFileTypeConverter : CommandLine.ITypeConverter<File> {
        override fun convert(str: String): File? {
            return if (str == "-") null else File(str)
        }
    }

    override fun run() {
        val sqlWriter = SQLWriter(targetCommName = commName, targetUserName = userName)
        val fileWriter = outfile?.bufferedWriter() ?: System.out.bufferedWriter()
        val fileReader = infile?.bufferedReader() ?: System.`in`.bufferedReader()

        System.err.println("Running...")
        fileWriter.use {
            fileReader.forEachLine { line ->
                val (post, comments) = DumpParser(line)
                fileWriter.append(sqlWriter(post, comments))
            }
        }
        System.err.println("Done!")
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(App()).execute(*args))
}