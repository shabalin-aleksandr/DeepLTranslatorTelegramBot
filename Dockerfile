FROM openjdk:17-jdk
ARG JAR_FILE=target/*.jar

# Put YOUR_BOT_NAME here
ENV BOT_NAME=DeepLTranslatorTelegramBot
# Put YOUR_BOT_TOKEN here
ENV BOT_TOKEN=5666007210:AAG_9PaDvdPU5TIAK-cwgkRyrGfNQaWXroY
ENV BOT_DB_URL=jdbc:postgresql://host.docker.internal:5432/deepl-telegram-bot
ENV BOT_DB_USERNAME=postgres
ENV BOT_DB_PASSWORD=786123

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Dbot.name=${BOT_NAME}", "-Dbot.token=${BOT_TOKEN}", "-Dspring.datasource.url=${BOT_DB_URL}", "-Dspring.datasource.username=${BOT_DB_USERNAME}", "-Dspring.datasource.password=${BOT_DB_PASSWORD}", "-jar", "app.jar"]
