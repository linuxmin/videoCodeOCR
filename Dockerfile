#Build:  docker build -t harb/ocr .
#Run: docker run --name ocr -t -i -p 8080:8080 harb/ocr

FROM alpine:edge

# install java
RUN \
  apk add --no-cache openjdk8

RUN apk update && apk add bash

# Install tesseract library
RUN apk add --no-cache tesseract-ocr

# Download last language package
RUN mkdir -p /usr/local/Cellar/tesseract/4.1.1/share/tessdata
ADD https://github.com/tesseract-ocr/tessdata/raw/master/ita.traineddata /usr/share/tessdata/ita.traineddata
RUN mv /usr/share/tessdata/eng.traineddata /usr/local/Cellar/tesseract/4.1.1/share/tessdata/


# Check the installation status
RUN tesseract --list-langs
RUN tesseract -v

# Set the location of the jar
ENV OCR_HOME /usr/ocrapp

ENV TESSDATA_PREFIX /usr/share/tessdata

#Install ffmpeg
RUN apk add  --no-cache ffmpeg

# Set the name of the jar
ENV APP_FILE OCR-1.0-SNAPSHOT.jar

# Open the port
EXPOSE 8080

# Copy our JAR
COPY target/$APP_FILE /app.jar

# Launch the Spring Boot application
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
