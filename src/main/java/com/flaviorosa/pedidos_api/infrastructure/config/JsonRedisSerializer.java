package com.flaviorosa.pedidos_api.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

public class JsonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper mapper;

    public JsonRedisSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // inclui o tipo Java no JSON — necessário para desserializar corretamente
        this.mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,  // ← era NON_FINAL
                JsonTypeInfo.As.PROPERTY
        );
    }

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) return new byte[0];
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Erro ao serializar para JSON", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return null;
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (IOException e) {
            throw new SerializationException("Erro ao desserializar do JSON", e);
        }
    }
}