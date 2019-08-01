FROM gradle:5.5.1-jdk8

# Copy Source
COPY . /usr/home/ffam/
WORKDIR /usr/home/ffam/

ENTRYPOINT ./gradlew bootRun --info