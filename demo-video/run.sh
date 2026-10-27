#!/bin/sh
cd "$(dirname "$0")" || exit 1
./mvnw -q -DskipTests package || exit 1
exec java -Dfile.encoding=UTF-8 -cp "target/classes:target/dependency/*" edu.java.issues.Launcher "$@"
