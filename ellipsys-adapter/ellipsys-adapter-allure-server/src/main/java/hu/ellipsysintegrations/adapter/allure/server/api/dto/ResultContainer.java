package hu.ellipsysintegrations.adapter.allure.server.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultContainer {

    @NonNull
    @JsonProperty(value = "file_name", required = true)
    private String fileName;

    @NonNull
    @JsonProperty(value = "content_base64", required = true)
    private String contentBase64;

}
