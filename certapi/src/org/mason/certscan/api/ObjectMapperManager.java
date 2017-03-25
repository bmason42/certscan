package org.mason.certscan.api;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @API_Spec_Description Object mapper for holding serialized timestamps.
 */
public class ObjectMapperManager {
    private static ObjectMapperManager instance = new ObjectMapperManager();
    private static ObjectMapper mapper;

    public static ObjectMapperManager getInstance() {
        return instance;
    }

    private ObjectMapperManager() {
    }

    public static synchronized ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper(new AllowCommentsJsonFactory());
            SimpleModule module = new SimpleModule("MasonModile", new Version(1, 0, 0, "alpha",null,null));
            module.addSerializer(long.class, new MyLongSerializer());
            module.addSerializer(Long.class, new MyLongSerializer());

            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.registerModule(module);
            mapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);

            final AnnotationIntrospector combinedIntrospector = createJaxbJacksonAnnotationIntrospector();
            mapper.setConfig(mapper.getDeserializationConfig().with(combinedIntrospector));


            mapper.registerModule(module);
        }
        return mapper;
    }

    private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector() {

        AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector();
        AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();

        return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
    }


    public static class MyLongSerializer extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeString(value.toString());
        }
    }
    static public class AllowCommentsJsonFactory extends JsonFactory {
        @Override
        public JsonParser createParser(URL url) throws IOException, JsonParseException {
            JsonParser p = super.createParser(url);
            p.enable(JsonParser.Feature.ALLOW_COMMENTS);
            return p;
        }
        @Override
        public JsonParser createParser(File url) throws IOException, JsonParseException {
            JsonParser p = super.createParser(url);
            p.enable(JsonParser.Feature.ALLOW_COMMENTS);
            return p;
        }
    }
}
