package hu.ellipsysintegrations.adapter.allure.server.api.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public abstract class SerializableDto {

    @SneakyThrows
    public String asJsonString() {
        return new ObjectMapper().writeValueAsString(this);
    }

}
