FROM java:alpine

MAINTAINER Oliver Gierke

# copy source into docker fs.
RUN ["mkdir","/project"]
WORKDIR /project

# first, download dependencies.
# see https://keyholesoftware.com/2015/01/05/caching-for-maven-docker-builds/
ADD pom.xml /project
ADD mvnw /project
ADD .mvn /project/.mvn
RUN ["./mvnw","verify","clean","--fail-never"]

# add rest of the project
ADD . /project

# test and create runnable jar.
RUN ["./mvnw","verify","package"]

# copy runnable jar to target container
RUN ["cp","./target/restbucks-1.0.0.BUILD-SNAPSHOT.jar","/app.jar"]

# To optimize docker image size, remove /project afterwards
# You can comment it out to keep maven inside docker from the
# "downloading the internet syndrome" during 
# development.
RUN ["rm","-rf","/project"]

WORKDIR /
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar","/app.jar"]
