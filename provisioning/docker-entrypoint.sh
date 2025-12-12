#!/bin/bash

JAVA_OPT="${JAVA_OPT:=-Xmx2g}"
APP_OPT="${APP_OPT:=}"

profile="${profile}"
project="${project}"
app="${app}"

cat application.yml
exec java ${JAVA_OPT} -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:./logs/gc-$(date +%F).log -Dfile.encoding=utf-8 -Dsentry.tags=app:${app} -jar ${app}.jar --spring.config.location=application.yml --spring.profiles.active=${profile} ${APP_OPT}
