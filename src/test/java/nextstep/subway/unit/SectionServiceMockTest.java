package nextstep.subway.unit;

import nextstep.exception.BadRequestException;
import nextstep.subway.line.Line;
import nextstep.subway.line.LineRepository;
import nextstep.subway.line.LineService;
import nextstep.subway.section.Section;
import nextstep.subway.section.SectionRepository;
import nextstep.subway.section.SectionRequest;
import nextstep.subway.section.SectionService;
import nextstep.subway.station.Station;
import nextstep.subway.station.StationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceMockTest {
    @InjectMocks
    SectionService sectionService;

    @InjectMocks
    LineService lineService;

    @Mock
    SectionRepository sectionRepository;

    @Mock
    LineRepository lineRepository;

    @Mock
    StationRepository stationRepository;

    @Test
    @DisplayName("구간에 역을 추가한다.")
    void addSection() {
        // given
        Line 이호선 = new Line(1L, "2호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");

        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));
        when(sectionRepository.save(any(Section.class))).then(AdditionalAnswers.returnsFirstArg());
        when(stationRepository.findById(강남역.getId())).thenReturn(Optional.of(강남역));
        when(stationRepository.findById(선릉역.getId())).thenReturn(Optional.of(선릉역));
        when(sectionRepository.findByUpStation(강남역)).thenReturn(Optional.ofNullable(null));
        when(sectionRepository.findByDownStation(선릉역)).thenReturn(Optional.ofNullable(null));

        // when
        SectionRequest sectionRequest = new SectionRequest(강남역.getId(), 선릉역.getId(), 7);
        sectionService.addSection(이호선, sectionRequest);

        // then
        assertThat(lineService.findLineById(이호선.getId()).getStations()).hasSize(2);
    }

    @Test
    @DisplayName("구간 중간에 역을 추가한다.")
    void addMiddleSection() {
        //given
        Line 이호선 = new Line(1L, "2호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");
        Station 신규역 = new Station(3L, "역삼역");

        Section 강남_선릉_구간 = new Section(1L, 강남역, 선릉역, 10, null); //기존

        SectionRequest sectionRequest = new SectionRequest(강남역.getId(), 신규역.getId(), 3);

        when(stationRepository.findById(강남역.getId())).thenReturn(Optional.of(강남역));
        when(stationRepository.findById(신규역.getId())).thenReturn(Optional.of(신규역));
        when(sectionRepository.findByUpStation(강남역)).thenReturn(Optional.of(강남_선릉_구간));
        when(sectionRepository.findByDownStation(신규역)).thenReturn(Optional.ofNullable(null));

        when(sectionRepository.save(any(Section.class))).then(AdditionalAnswers.returnsFirstArg());
        when(sectionRepository.findByDownStation(강남역)).thenReturn(Optional.ofNullable(null));

        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        //when
        sectionService.addSection(이호선, sectionRequest);

        //then
        assertThat(lineService.findLineById(이호선.getId()).getStations()).hasSize(3);
    }

    @Test
    @DisplayName("신규 구간을 추가할 기존 구간을 찾을때 추가하려는 구간과 같은 경우 예외가 발생한다.")
    void sameSectionException() {
        Line 이호선 = new Line(1L, "이호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");

        Section 기존_구간 = new Section(1L, 강남역, 선릉역, 10, null);
        Section 등록할_구간 = new Section(2L, 강남역, 선릉역, 10, null);

        when(sectionRepository.save(any(Section.class))).then(AdditionalAnswers.returnsFirstArg());
        when(stationRepository.findById(강남역.getId())).thenReturn(Optional.of(강남역));
        when(stationRepository.findById(선릉역.getId())).thenReturn(Optional.of(선릉역));
        when(sectionRepository.findByUpStation(강남역)).thenReturn(Optional.of(기존_구간));
        when(sectionRepository.findByDownStation(선릉역)).thenReturn(Optional.ofNullable(null));

        //when & then
        assertThatThrownBy(() -> sectionService.addSection(이호선, new SectionRequest(등록할_구간)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("신규 구간을 추가할 기존 구간을 찾을 때 기존 구간 보다 신규 구간이 길면 예외가 발생한다.")
    void overDistanceSectionException() {
        Line 이호선 = new Line(1L, "이호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");
        Station 역삼역 = new Station(3L, "역삼역");

        Section 기존_구간 = new Section(1L, 강남역, 선릉역, 10, null);
        Section 등록할_구간 = new Section(2L, 강남역, 역삼역, 13, null);

        when(sectionRepository.save(any(Section.class))).then(AdditionalAnswers.returnsFirstArg());
        when(stationRepository.findById(강남역.getId())).thenReturn(Optional.of(강남역));
        when(stationRepository.findById(역삼역.getId())).thenReturn(Optional.of(역삼역));
        when(sectionRepository.findByUpStation(강남역)).thenReturn(Optional.of(기존_구간));
        when(sectionRepository.findByDownStation(역삼역)).thenReturn(Optional.ofNullable(null));

        //when & then
        assertThatThrownBy(() -> sectionService.addSection(이호선, new SectionRequest(등록할_구간)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("구간 맨 앞에 역을 추가한다.")
    void addFirstSection() {
        //given
        Line 이호선 = new Line(1L, "2호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");
        Station 신규역 = new Station(3L, "서초역");

        Section 강남_선릉_구간 = new Section(1L, 강남역, 선릉역, 10, null); //기존
        이호선.addEndSection(강남_선릉_구간);

        SectionRequest sectionRequest = new SectionRequest(신규역.getId(), 강남역.getId(),3);

        when(stationRepository.findById(강남역.getId())).thenReturn(Optional.of(강남역));
        when(stationRepository.findById(신규역.getId())).thenReturn(Optional.of(신규역));
        when(sectionRepository.findByDownStation(강남역)).thenReturn(Optional.ofNullable(null));

        when(sectionRepository.save(any(Section.class))).then(AdditionalAnswers.returnsFirstArg());
        when(sectionRepository.findByUpStation(신규역)).thenReturn(Optional.ofNullable(null));
        when(sectionRepository.findByUpStation(강남역)).thenReturn(Optional.of(강남_선릉_구간));

        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        //when
        sectionService.addSection(이호선, sectionRequest);

        //then
        assertThat(lineService.findLineById(이호선.getId()).getStations()).hasSize(3);
    }

    @Test
    @DisplayName("역이 3개 존재하는 노선의 2번째 역을 삭제한다.")
    void deleteMiddleStation() {
        //given
        Line 이호선 = new Line(1L, "2호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");
        Station 삼성역 = new Station(3L, "삼성역");

        Section 강남_선릉_구간 = new Section(1L, 강남역, 선릉역, 10, 2L); //기존
        이호선.addEndSection(강남_선릉_구간);
        Section 선릉_삼성_구간 = new Section(2L, 선릉역, 삼성역, 5, null);
        이호선.addEndSection(선릉_삼성_구간);

        when(sectionRepository.findByUpStation(선릉역)).thenReturn(Optional.of(선릉_삼성_구간));
        when(sectionRepository.findByDownStation(선릉역)).thenReturn(Optional.of(강남_선릉_구간));

        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        sectionService.deleteSection(이호선, 선릉역);

        assertThat(lineService.findLineById(이호선.getId()).getStations()).hasSize(2);
    }

    @Test
    @DisplayName("역이 4개 존재하는 노선의 3번째 역을 삭제한다.")
    void deleteMiddleStation2() {
        //given
        Line 이호선 = new Line(1L, "2호선", "green");
        Station 강남역 = new Station(1L, "강남역");
        Station 선릉역 = new Station(2L, "선릉역");
        Station 삼성역 = new Station(3L, "삼성역");
        Station 잠실역 = new Station(4L, "잠실역");

        Section 강남_선릉_구간 = new Section(1L, 강남역, 선릉역, 10, 2L); //기존
        이호선.addEndSection(강남_선릉_구간);
        Section 선릉_삼성_구간 = new Section(2L, 선릉역, 삼성역, 5, 3L);
        이호선.addEndSection(선릉_삼성_구간);
        Section 삼성_잠실_구간 = new Section(3L, 삼성역, 잠실역, 4, null);
        이호선.addEndSection(삼성_잠실_구간);

        when(sectionRepository.findByUpStation(삼성역)).thenReturn(Optional.of(삼성_잠실_구간));
        when(sectionRepository.findByDownStation(삼성역)).thenReturn(Optional.of(선릉_삼성_구간));
        when(sectionRepository.findByDownStation(선릉역)).thenReturn(Optional.of(강남_선릉_구간));

        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        sectionService.deleteSection(이호선, 삼성역);

        assertAll(
                () -> assertThat(lineService.findLineById(이호선.getId()).getStations()).hasSize(3),
                () -> assertThat(강남_선릉_구간.getNextSectionId()).isEqualTo(3L),
                () -> assertThat(삼성_잠실_구간.getUpStation()).isEqualTo(선릉역)
        );
    }
}
