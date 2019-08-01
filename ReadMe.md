FFAM Work Distribution API Service

About :

- This is a Java Springboot Application exposes Rest APIs to create and complete tasks among a given set of agents.
- Requires Java 8 or greater or Docker (See section to run using docker-compose)

Monitoring :

- Checking if the Service is UP and Running :
  - http://localhost:8081/actuator/health should return {"status":"UP"}

Running the Service using Gradle :

- Running the Application as a Spring Boot App from Gradle :
  - Make sure no other services are running on port 8081 and port 1521
  - Change the port to a FREE_PORT in the DB_URL of application.yaml
  - If 8081 is taken, then change application.yml file to make the App listen on a different port
  - If 1521 is taken, run the docker run -d -p <FREE_PORT>:1521 mydb:latest listen on a different port

  - Pre-requisites :
    - Make sure that the database is up and running.
    - Do a docker ps to see if the container with image mydb is running
    - If not :
        - Build the DB Image :
          - cd into docker/oracle
          - docker build -t mydb .
          - docker run -d -p 1521:1521 mydb:latest
  - Once the DB is up and running, do ./gradlew clean bootRun

- Running Unit Tests : ./gradlew clean unitTests

- Running All Tests : 
  - All tests include also running integration tests with the database. 
  - Note that since at this point, we use a singular schema, this tests will clean up the DB once done
  - To run all tests : ./gradlew clean test 

Running the Service using Docker Compose :
- This app can also be run using Docker Compose.
- There is a wrapper script that is part of /compose folder
- To run the Application and the Database using compose
  - cd into the compose folder
  - To start the Services :
    - ./environment.sh start
    - This can also be done using series of compose commands
      - docker-compose -f ffam.yaml down --remove-orphans
      - docker-compose -f ffam.yaml build
      - docker-compose -f ffam.yaml up
  - To stop the Service :
    - ./environment stop or docker-compose -f ffam.yaml down --remove-orphans
- Once the start command is Run, wait for the service to completely come up by using the Monitoring Instructions above



