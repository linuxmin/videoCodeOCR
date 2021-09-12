# videoCodeOCR

OCR Processing of Source Code within Video Files

Deployment:

Clone the repository

Open a terminal and change to the project's root directory

Run "mvn clean install" to execute the clean and install Maven goals

Run "docker build -t harb/ocr ." to build the docker image (this can take a while, use "-{}-progress plain" as parameter
for verbose progress output)

Run "docker run --name ocr -t -i -p 8083:8083 harb/ocr" to run the container

Restart the container by running "docker stop ocr" and then running "docker start ocr -a" (not restarting the container
bugs the download function, no other solution to this problem could be found)