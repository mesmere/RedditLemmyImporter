This project translates Reddit API responses into a PL/pgSQL script which loads the data into a [Lemmy](https://github.com/LemmyNet/lemmy/) database.

In other words, this is a tool that ![takes](https://user-images.githubusercontent.com/95945959/166644376-d165623c-6d1c-4cb6-80e9-918040968446.png) Reddit posts/comments and ![puts](https://user-images.githubusercontent.com/95945959/166644445-e29e1b37-51d2-4a81-ab4f-fb30efd39f10.png) them into Lemmy.

## Screenshots

Community|Post
---|---
![comm screenshot](https://user-images.githubusercontent.com/95945959/166649549-1d4eddfc-2a4e-4b83-a8c4-ef5935584b30.png)|![post screenshot](https://user-images.githubusercontent.com/95945959/166649995-df61648f-4346-4d6d-8545-ad26414cbd7d.png)

## Getting input data

To get the JSON API response for a single post, you can call [the proper Reddit API](https://www.reddit.com/dev/api/#GET_comments_{article}) (requires an API key), or just append `.json` to the comments URL, like this:

```
HTML: https://www.reddit.com/r/GenZedong/comments/laucjl/china_usa/
      https://www.reddit.com/r/GenZedong/comments/laucjl

JSON: https://www.reddit.com/r/GenZedong/comments/laucjl/china_usa/.json?limit=10000
      https://www.reddit.com/r/GenZedong/comments/laucjl.json?limit=10000
```

Note that we've also added the `limit` parameter, because otherwise Reddit will pretty aggressively prune the comment tree with "Load more comments" links.

The response object contains the data for that one post and any replies. You can feed this directly into RedditLemmyImporter. However, if you want to import multiple posts, you can put multiple responses in the same input file, with each one separated by a newline. For example:

```
~ $ cat urls
https://www.reddit.com/r/GenZedong/comments/tpyft9/why_is_like_half_this_sub_made_of_trans_women/
https://www.reddit.com/r/GenZedong/comments/pet8zc/therapist_trans_stalin_isnt_real_she_cant_hurt/
https://www.reddit.com/r/GenZedong/comments/ttcyok/happy_trans_visibility_day_comrades/
https://www.reddit.com/r/GenZedong/comments/t9kbdm/women_of_genzedong_i_congratulate_you_for_your_day/
~ $ xargs -I URL curl --silent --user-agent "Subreddit archiver" --cookie "REDACTED" URL.json?limit=10000 < urls > dump.json
```

## Cloning an entire subreddit

If you need a complete scraping solution, check out [this Python script](https://lemmygrad.ml/post/187006/comment/130292). It pulls posts into a local MongoDB database, which means you can run it on a cron to keep a local clone of posts as they're made. To export your `dump.json` try something like this:

```
mongoexport --uri="mongodb://localhost:27017/subredditArchiveDB" --collection=GenZedong --out=dump-wrapped.json
```

Note that the script will bury the data we need within a top-level property named `json`. RedditLemmyImporter can handle this directly using the `--json-pointer` option. For example:

```
java -jar redditLemmyImporter-0.3.jar -c genzhouarchive -u archive_bot -o import.sql --json-pointer=/json GenZhouArchive.json
```

## Generating a SQL script using the release binary

Prerequisites: Java 8 or above

Download the jar file from the [releases page](https://github.com/rileynull/RedditLemmyImporter/releases) and run it:

```
java -jar redditLemmyImporter-0.3.jar -c genzhouarchive -u archive_bot -o import.sql dump.json
```

In this case we're generating a PL/pgSQL script that will load the data from `dump.json` into the comm `genzhouarchive` under the user `archive_bot`. The script will be written to `import.sql`. Full command usage:

```
Usage: redditLemmyImporter [OPTIONS] dump
      dump                   Path to the JSON dump file from the Reddit API. Required.
                             Specify - to read from stdin.
  -c, --comm=name            Target community name. Required.
  -u, --user=name            Target user name. Required.
      --json-pointer=pointer Locate the Reddit API response somewhere within the top-level object in each input line.
                             See RFC 6901 for the JSON Pointer specification.
  -o, --output-file=file     Output file. Prints to stdout if this option isn't specified.
  -h, --help                 Show this help message and exit.
  -V, --version              Print version information and exit.
```

## Generating a SQL script using the source repository

Prerequisites: JDK >=1.8, Maven 3. 

Clone the repo and cd to the source tree. Run:

```
mvn compile
mvn exec:java -Dexec.args="-c genzhouarchive -u archive_bot -o import.sql path/to/dump.json"
```

(This will pull down dependencies from Maven Central so you must be connected to the internet during the compile step.)

You could also package a release and then follow the instructions from the previous section:

```
mvn clean package
java -jar target/redditLemmyImporter-0.3-SNAPSHOT.jar -c genzhouarchive -u archive_bot -o import.sql dump.json
```

## Running the SQL script

Copy `import.sql` to the server running Postgres and run this:

```
psql --dbname=lemmy --username=lemmy --file=import.sql
```

Note that this uses the default values for the database name and database username. If you've changed them in your [Lemmy configuration](https://join-lemmy.org/docs/en/administration/configuration.html#full-config-with-default-values) then update the values accordingly.

**The target comm and target user must already exist in your Lemmy instance or the SQL script will do nothing.**

## Running the SQL script with Dockerized Lemmy

Copy `import.sql` to the server running Docker and run this:

```
<import.sql docker exec -i $(docker ps -qf name=postgres) psql --dbname=lemmy --username=lemmy -
```

**The target comm and target user must already exist in your Lemmy instance or the SQL script will do nothing.**

## Sample data

/r/GenZhou was scraped by `@DongFangHong@lemmygrad.ml`, with data available up to about a week before it was banned:  
https://mega.nz/file/knBwmTJL#PpqO0I3Jv-xw-o7RBWSi0JSScjSV7-4Eb3JR5HzTc5w

This data file was generated by the Python script linked above, which means the data we need on each line is inside a top-level property named `json`. Use the `--json-pointer` option [as described above](#cloning-an-entire-subreddit) when importing this JSON file.
