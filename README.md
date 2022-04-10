This project takes a JSON archive scraped from /r/GenZhou (before it was banned) and translates each post into a PL/pgSQL script which will insert the data into a Lemmy database for rehosting elsewhere.

You can download the generated SQL script from the repo's [releases page](https://github.com/rileynull/GenZhouImporter/releases) and run it directly. It's hardcoded to add posts to c/genzhouarchive under the user u/archive_bot, both of which **must already exist** before running the script.

If you want to modify the default community name or user name, you're going to have to run the code to generate the SQL script yourself. This is a good idea anyway since it's not wise to trust me to run arbitrary queries on your database, and in this case the easiest way to review them is to review the code that generates them.

## Screenshot

[![screenshot](https://user-images.githubusercontent.com/95945959/162605469-43d34fdf-3559-4017-81c8-486d9b280a1b.png)](https://user-images.githubusercontent.com/95945959/162605437-8cb64245-2048-4bf0-afd7-6de3a2d32a29.png)
(click/tap to expand)

## Generating the SQL script from the release binary

Prerequisites: Java 8 or above

Download the jar file from the [releases page](https://github.com/rileynull/GenZhouImporter/releases) and run it:

```
# Args: community name, then user name
java -jar genZhouImporter-0.2-SNAPSHOT.jar genzhouarchive archive_bot
```

The output file `GenZhouArchive.sql` will be placed in your current working directory.

## Generating the SQL script from source

Prerequisites: JDK >=1.8, Maven 3. 

Clone the repo and cd to the GenZhouImporter directory. Run:

```
mvn compile
mvn exec:java -Dexec.args="genzhouarchive archive_bot"
```

(This will pull down dependencies from Maven Central so you must be connected to the internet during the compile step.)

The output file `GenZhouArchive.sql` will be placed in your current working directory.

## Running the SQL script

(Note that this uses the default values for the database name and database username. If you've changed them in your [Lemmy configuration](https://join-lemmy.org/docs/en/administration/configuration.html#full-config-with-default-values) then update the values accordingly.)

Copy `GenZhouArchive.sql` to the server running Postgres and run this:

```
psql --dbname=lemmy --username=lemmy --file=GenZhouArchive.sql
```

## Running the SQL script with Dockerized Lemmy

(Note that this uses the default values for the database name and database username. If you've changed them in your [Lemmy configuration](https://join-lemmy.org/docs/en/administration/configuration.html#full-config-with-default-values) then update the values accordingly.)

Copy `GenZhouArchive.sql` to the server running Docker and run this:

```
cat GenZhouArchive.sql | docker exec -i $(docker ps -qf name=postgres) psql --dbname=lemmy --username=lemmy
```

## Credits

The input data was scraped by `@DongFangHong@lemmygrad.ml` probably some time on 30 March 2022. (For reference, the subreddit was banned on 4 April.)
