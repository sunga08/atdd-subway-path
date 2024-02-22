package nextstep.subway.section;

import nextstep.subway.line.Line;
import nextstep.subway.station.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByDownStation(Station station);
    Optional<List<Section>> findByLine(Line line);
}
