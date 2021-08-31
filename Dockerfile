#Build:  docker build -t harb/ocr .
#Run: docker run --name ocr -t -i -p 8083:8083 harb/ocr

FROM alpine:edge

# install java 11
RUN \
  apk add --no-cache openjdk11

RUN apk update && apk add bash

#Setting needed environment variables
ENV OCR_HOME /usr/ocrapp
ENV TESSDATA_PREFIX /usr/share/tessdata
ENV LD_LIBRARY_PATH /usr/lib
ENV APP_FILE OCR-1.0-SNAPSHOT.jar
ENV JAVA_OPTS=""

# Install tesseract library
RUN apk add --no-cache tesseract-ocr-dev

# Download last language package
RUN mkdir -p /usr/local/Cellar/tesseract/4.1.1/share/tessdata
ADD https://github.com/tesseract-ocr/tessdata/raw/master/ita.traineddata /usr/share/tessdata/ita.traineddata
RUN mv /usr/share/tessdata/eng.traineddata /usr/local/Cellar/tesseract/4.1.1/share/tessdata/eng.traineddata

# Check the installation status
RUN tesseract --list-langs
RUN tesseract -v

#Install ffmpeg
RUN apk add  --no-cache ffmpeg

#Install graphviz for plantuml
RUN apk add --no-cache graphviz

#Install ttf-dejavu (needed for plantuml png generation, see https://forum.plantuml.net/9781/invocationtargetexception-fileformat-getjavadimension)
RUN apk add  --no-cache ttf-dejavu

#Install git and clone micro-service and monolith sales app
RUN apk add  --no-cache git
RUN git clone https://github.com/debbienuta/microservice-sales-app.git

# Open the port
EXPOSE 8083

# Copy our JAR
COPY target/$APP_FILE /app.jar

#Launch the Spring Boot application
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=server -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
