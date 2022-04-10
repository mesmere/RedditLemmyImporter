# GenZhouImporter

This project takes a JSON archive scraped from /r/GenZhou (before it was banned) and translates each post into a PL/pgSQL script which will insert the data into a Lemmy database for rehosting elsewhere.

You can download the generated SQL script from the repo's [releases page](https://github.com/rileynull/GenZhouImporter/releases) and run it directly. It's hardcoded to add posts to c/genzhouarchive under the user u/archive_bot, both of which **must already exist** before running the script.

If you want to modify the default community name or user name, you're going to have to run the code to generate the SQL script yourself. This is a good idea anyway since it's not wise to trust me to run arbitrary queries on your database, and in this case the easiest way to review them is to review the code that generates them.

## Generating the SQL script from the release binary:

Prerequisites: Java 8 or above

Download the jar file from the [releases page](https://github.com/rileynull/GenZhouImporter/releases) and run it:

```sh
java -jar genZhouImporter-0.1-SNAPSHOT.jar
```

If you don't want to use the default values for the community name and user name, you can supply additional arguments on the command line:

```sh
java -jar genZhouImporter-0.1-SNAPSHOT.jar genzhouarchive archive_bot
```

Either way, the output file `GenZhouArchive.sql` will be placed in your current working directory.

## Generating the SQL script from source:

Prerequisites: JDK >=1.8, Maven 3. 

Clone the repo and cd to the GenZhouImporter directory. Run:

```sh
mvn compile
mvn exec:java
```

(This will pull down dependencies from Maven Central so you must be connected to the internet during the compile step.

If you don't want to use the default values for the community name and user name, you can supply additional arguments on the command line:

```sh
mvn compile
mvn exec:java -Dexec.args="genzhouarchive archive_bot"
```

Either way, the output file `GenZhouArchive.sql` will be placed in your current working directory.

## Running the SQL script:

(Note that this uses the default values for the database name and database username. If you've changed them in your [Lemmy configuration](https://join-lemmy.org/docs/en/administration/configuration.html#full-config-with-default-values) then update the values accordingly.)

SSH to the server running postgres and run this:

```sh
psql --dbname=lemmy --username=lemmy --file=GenZhouArchive.sql
```

It should prompt you for your database password; the default is `password`

## Running the SQL script when Lemmy is running in Docker:

(Note that this uses the default values for the database name and database username. If you've changed them in your [Lemmy configuration](https://join-lemmy.org/docs/en/administration/configuration.html#full-config-with-default-values) then update the values accordingly.)

SSH to the server running docker and run this to copy the SQL file into the container and run it:

```sh
docker cp ./GenZhouArchive.sql $(docker ps -qf name=postgres):/var/lib/postgresql
docker exec -it $(docker ps -qf name=postgres) psql --dbname=lemmy --username=lemmy --file=/var/lib/postgresql/GenZhouArchive.sql
```

No password is necessary because you're connecting locally through a Unix socket. It should take less than a minute to run.

## Credits

The input data was scraped by `@DongFangHong@lemmygrad.ml` probably some time on 30 March 2022. (For reference, the subreddit was banned on 4 April.)
