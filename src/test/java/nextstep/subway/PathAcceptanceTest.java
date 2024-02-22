package nextstep.subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.common.CommonAcceptanceTest;
import nextstep.subway.common.LineRestAssuredCRUD;
import nextstep.subway.common.SectionRestAssuredCRUD;
import nextstep.subway.common.StationRestAssuredCRUD;
import nextstep.subway.path.PathResponse;
import nextstep.subway.station.Station;
import nextstep.subway.station.StationResponse;
import org.apache.commons.lang3.builder.StandardToStringStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PathAcceptanceTest extends CommonAcceptanceTest {

    Long extractResponseId(ExtractableResponse<Response> response) {
        return response.body().jsonPath().getLong("id");
    }

    @Test
    @DisplayName("경로조회")
    void findPath() {
        int 교대역_남부터미널역_거리 = 3;
        int 남부터미널역_양재역_거리 = 4;

        ExtractableResponse<Response> lineResponse = LineRestAssuredCRUD.createLine("3호선", "bg-orange-600");
        Long 삼호선Id = lineResponse.jsonPath().getLong("id");

        Long 교대역Id = extractResponseId(StationRestAssuredCRUD.createStation("교대역"));
        Long 남부터미널역Id = extractResponseId(StationRestAssuredCRUD.createStation("남부터미널역"));
        Long 양재역Id = extractResponseId(StationRestAssuredCRUD.createStation("양재역"));

        SectionRestAssuredCRUD.addSection(교대역Id, 남부터미널역Id, 교대역_남부터미널역_거리, 삼호선Id);
        SectionRestAssuredCRUD.addSection(남부터미널역Id, 양재역Id, 남부터미널역_양재역_거리, 삼호선Id);

        //when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .param("source", 교대역Id)
                    .param("target", 양재역Id)
                .when()
                    .get("/path")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        // then
        PathResponse pathResponse = response.as(PathResponse.class);
        List<Long> ids = response.jsonPath().getList("stations.id", Long.class);
        int distance = response.jsonPath().getInt("distance");

        assertThat(ids).containsExactly(교대역Id, 남부터미널역Id, 양재역Id);
        assertThat(pathResponse.getDistance()).isEqualTo(교대역_남부터미널역_거리 + 남부터미널역_양재역_거리);
    }
}
