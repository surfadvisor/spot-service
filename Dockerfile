FROM openjdk:11-slim

MAINTAINER Daniel Poznański "misterdannypl@gmail.com"

ENV PORT 8080

EXPOSE 8080

ADD app.jar app.jar

CMD ["java","-Xmx256m","-jar","app.jar"]
