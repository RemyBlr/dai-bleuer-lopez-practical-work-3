# Use a base image with OpenJDK
FROM alpine:3.18

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/dai-bleuer-lopez-practical-work-3-1.0-SNAPSHOT.jar /app/dai-bleuer-lopez-practical-work-3-1.0-SNAPSHOT.jar

# Expose the necessary ports
EXPOSE 9876/udp
EXPOSE 9877/udp

# Define the command to run your application
CMD ["java", "-jar", "dai-bleuer-lopez-practical-work-3-1.0-SNAPSHOT.jar"]
