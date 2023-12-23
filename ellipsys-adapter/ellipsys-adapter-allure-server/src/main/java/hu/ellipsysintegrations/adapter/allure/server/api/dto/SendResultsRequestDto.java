package hu.ellipsysintegrations.adapter.allure.server.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendResultsRequestDto extends SerializableDto {

    @NonNull
    @JsonProperty(value = "results", required = true)
    @Singular
    private List<ResultContainer> results;

}
