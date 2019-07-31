FROM openjdk:8

# Copy Source
COPY . /usr/home/ffam/
WORKDIR /usr/home/ffam/

ENTRYPOINT gradle bootRun --info