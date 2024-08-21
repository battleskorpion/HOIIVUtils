# Use OpenJDK 22 as the base image
FROM openjdk:22

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file into the container
COPY target/HOIIVUtils-11.0.0-SNAPSHOT-jar-with-dependencies.jar /app/HOIIVUtils-11.0.0-SNAPSHOT-jar-with-dependencies.jar

# Run the application
CMD ["java", "-jar", "HOIIVUtils-11.0.0-SNAPSHOT-jar-with-dependencies.jar"]