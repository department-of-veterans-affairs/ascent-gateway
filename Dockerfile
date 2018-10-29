FROM ascent/ascent-base

ENV JAR_FILE "/ascent-gateway.jar"
ADD target/ascent-gateway.jar $JAR_FILE


# Append app specific secrets to load to the base config
 RUN echo \
 'secret { \
     format = "ascent.gateway.{{ key }}" \
     no_prefix = true \
     path = "secret/ascent-gateway" \
 }' >> $ENVCONSUL_CONFIG
