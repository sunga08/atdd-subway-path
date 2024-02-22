package nextstep.subway.path;

import nextstep.exception.BadRequestException;
import nextstep.subway.line.Line;
import nextstep.subway.line.LineRepository;
import nextstep.subway.section.Section;
import nextstep.subway.section.SectionRepository;
import nextstep.subway.station.Station;
import nextstep.subway.station.StationRepository;
import nextstep.subway.station.StationResponse;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class PathService {
    private LineRepository lineRepository;
    private SectionRepository sectionRepository;
    private StationRepository stationRepository;

    public PathService(LineRepository lineRepository, SectionRepository sectionRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.sectionRepository = sectionRepository;
        this.stationRepository = stationRepository;
    }

    public PathResponse findPath(Long source, Long target) {
        Station startStation = stationRepository.findById(source).orElseThrow(
                () -> new BadRequestException("존재하지 않는 출발역입니다.")
        );
        Station endStation = stationRepository.findById(target).orElseThrow(
                () -> new BadRequestException("존재하지 않는 도착역입니다.")
        );

        List<Line> lines = lineRepository.findAll();

        WeightedMultigraph<Station, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);

        for(Line line : lines) {
            List<Station> stations = line.getStations();
            for(Station station : stations) {
                if(!graph.containsVertex(station)) {
                    graph.addVertex(station);
                }
            }

            List<Section> sections = line.getSectionList();
            for(Section section : sections) {
                Station upStation = section.getUpStation();
                Station downStation = section.getDownStation();
                if(graph.containsVertex(upStation) && graph.containsVertex(downStation) && !graph.containsEdge(upStation, downStation)) {
                    DefaultWeightedEdge edge = graph.addEdge(upStation, downStation);
                    graph.setEdgeWeight(edge, section.getDistance());
                }
            }
        }

        GraphPath shortestPath = new DijkstraShortestPath(graph).getPath(startStation, endStation);

        List<Station> shortestPathStations = shortestPath.getVertexList();
        double shortestPathWeight = shortestPath.getWeight();

        PathResponse pathResponses = PathResponse.of(shortestPathStations, (int) shortestPathWeight);

        return pathResponses;
    }
}
