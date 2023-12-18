# Start from the Java 17 Temurin image
FROM eclipse-temurin:17

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/dai-bleuer-lopez-practical-work-3-1.0-SNAPSHOT.jar /app/dai-bleuer-lopez-practical-work-3-1.0-SNAPSHOT.jar

# Expose the necessary ports
EXPOSE 9876/udp
EXPOSE 9877/udp

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "dai-bleuer-lopez-practical-work-3-1.0-SNAPSHOT.jar"]

# Set the default command
CMD ["--help"]
