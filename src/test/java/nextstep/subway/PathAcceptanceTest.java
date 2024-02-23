package nextstep.subway;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.common.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PathAcceptanceTest extends CommonAcceptanceTest {
    private Long 교대역Id;
    private Long 남부터미널역Id;
    private Long 양재역Id;

    private static int 교대역_남부터미널역_거리 = 3;
    private static int 남부터미널역_양재역_거리 = 4;

    void setLine() {
        ExtractableResponse<Response> lineResponse = LineRestAssuredCRUD.createLine("3호선", "bg-orange-600");
        Long 삼호선Id = lineResponse.jsonPath().getLong("id");

        교대역Id = extractResponseId(StationRestAssuredCRUD.createStation("교대역"));
        남부터미널역Id = extractResponseId(StationRestAssuredCRUD.createStation("남부터미널역"));
        양재역Id = extractResponseId(StationRestAssuredCRUD.createStation("양재역"));

        SectionRestAssuredCRUD.addSection(교대역Id, 남부터미널역Id, 교대역_남부터미널역_거리, 삼호선Id);
        SectionRestAssuredCRUD.addSection(남부터미널역Id, 양재역Id, 남부터미널역_양재역_거리, 삼호선Id);
    }

    @Test
    @DisplayName("경로조회")
    void findPath() {
        //given
        setLine();

        //when
        ExtractableResponse<Response> response = PathRestAssuredCRUD.showPath(교대역Id, 양재역Id);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        // then
        List<Long> stationIds = response.jsonPath().getList("stations.id", Long.class);
        double distance = response.jsonPath().getDouble("distance"); //PathResponse에는 int로 설정했음에도 응답값은 double 형식으로 리턴..

        assertThat(stationIds).containsExactly(교대역Id, 남부터미널역Id, 양재역Id);
        assertThat(distance).isEqualTo(7.0);
    }

    Long extractResponseId(ExtractableResponse<Response> response) {
        return response.body().jsonPath().getLong("id");
    }
}
