package gov.va.ascent.gateway.audit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;



@EnableAsync
@Component
public class AscentGatewayAuditHelper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AscentGatewayAuditHelper.class);
	@Autowired Tracer tracer;

	@Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }
	
	@Async
	public void addTracerTagsAsync(String responseMessage) {
		try{
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(responseMessage);
			LOGGER.debug("JsonElement:: {}", jsonElement);
			if (!jsonElement.isJsonNull()) {
				if (jsonElement.isJsonPrimitive()) {
					this.tracer.addTag(jsonElement.getAsString(), jsonElement.getAsString());
				}
				if (jsonElement.isJsonObject()) {
					final JsonObject jsonObject = jsonElement.getAsJsonObject();
					processJsonObject(jsonObject);
				}
			}
		}
		catch(JsonSyntaxException jse){
			LOGGER.warn("Not a valid Json String: {}", jse.getMessage());
			LOGGER.warn("Non valid Json String Exception: {}", jse);
		}
	}
	
	private void processJsonObject(final JsonObject jsonObject) {
		LOGGER.debug("JsonObject:: {}", ReflectionToStringBuilder.toString(jsonObject));
		if (jsonObject != null) {
			for(Map.Entry<String,JsonElement> att : jsonObject.entrySet()) {
				String elementKey = att.getKey();
				JsonElement elementValue = att.getValue();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Key >>> {}", elementKey);
					LOGGER.debug("Val >>> {}", elementValue);
				}
				if(elementValue.isJsonPrimitive()) {
					addTracer(elementKey, elementValue);
				}
				if(elementValue.isJsonObject()) {
					for(Map.Entry<String,JsonElement> jsonElement : elementValue.getAsJsonObject().entrySet()){
						processJsonElement(jsonElement.getKey(), jsonElement.getValue());
					}
				}
				if(elementValue.isJsonArray()) {
					processJsonArray(elementValue.getAsJsonArray());
				}
	        }
		}
	}
	
	private void processJsonArray(final JsonArray jsonArray) {
		LOGGER.debug("JsonArray:: {}", ReflectionToStringBuilder.toString(jsonArray));
		if (jsonArray != null) {
			final Iterator<JsonElement> jsonElement = jsonArray.iterator();
			int index = 0;
			while (jsonElement.hasNext()) {
				processJsonElement(String.valueOf(index), jsonElement.next());
				index++;
			}
		}
	}
	
	private void processJsonElement(final String key, final JsonElement jsonElement) {
		LOGGER.debug("processJsonElement#JsonElement:: {}", ReflectionToStringBuilder.toString(jsonElement));
		if (jsonElement.isJsonPrimitive()) {
			this.tracer.addTag(key, jsonElement.getAsString());
		}
		if (jsonElement.isJsonArray()) {
			processJsonArray(jsonElement.getAsJsonArray());
		}
		if (jsonElement.isJsonObject()) {
			for(Map.Entry<String,JsonElement> elementlevel : jsonElement.getAsJsonObject().entrySet()) {
				final String elementKey = elementlevel.getKey();
				JsonElement elementValue = elementlevel.getValue();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.info("Key >>> {}", elementKey);
					LOGGER.info("Value >>> {}", elementValue);
				}
				if (elementValue.isJsonPrimitive()) {
					addTracer(elementKey, elementValue);
				}
				if (elementValue.isJsonObject()) {
					processJsonObject(elementValue.getAsJsonObject());
				}
				if(elementValue.isJsonArray()) {
					processJsonArray(elementValue.getAsJsonArray());
				}
			}
		}
	}
	
	private void addTracer(final String elementKey, final JsonElement elementValue) {
		this.tracer.addTag(elementKey, elementValue.getAsString());
	}

}