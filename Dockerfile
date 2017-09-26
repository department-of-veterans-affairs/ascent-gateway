FROM ascent/ascent-base

ENV JAR_FILE "/ascent-gateway.jar"
ADD target/ascent-gateway.jar $JAR_FILE