FROM java:8
ADD target/ascent-gateway-*.jar /ascent-gateway.jar
ENTRYPOINT ["java", "-Xms64m", "-Xmx256m", "-jar", "/ascent-gateway.jar"]
