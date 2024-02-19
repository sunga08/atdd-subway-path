package nextstep.subway.section;

import nextstep.exception.BadRequestException;
import nextstep.subway.line.Line;
import nextstep.subway.station.Station;
import nextstep.subway.station.StationRepository;
import org.springframework.stereotype.Service;

@Service
public class SectionService {
    private final SectionRepository sectionRepository;

    private final StationRepository stationRepository;

    public SectionService(SectionRepository sectionRepository, StationRepository stationRepository) {
        this.sectionRepository = sectionRepository;
        this.stationRepository = stationRepository;
    }

    public Section validSection(SectionRequest sectionRequest) {
        Station upStation = stationRepository.findById(sectionRequest.getUpStationId()).orElseThrow(
                () -> new BadRequestException("존재하지 않는 역입니다.")
        );
        Station downStation = stationRepository.findById(sectionRequest.getDownStationId()).orElseThrow(
                () -> new BadRequestException("존재하지 않는 역입니다.")
        );

        Section requestSection = new Section(upStation, downStation, sectionRequest.getDistance());

        Section section = sectionRepository.findByDownStation(downStation).orElse(null);
        if(section != null) {
            throw new BadRequestException("노선에 추가할 수 없는 구간입니다.");
        }

        return requestSection;
    }

    public void addSection(Line line, SectionRequest sectionRequest) {
        Section requestSection = validSection(sectionRequest);
        Section existingSection = sectionRepository.findByUpStation(requestSection.getUpStation()).orElse(null);
        requestSection = sectionRepository.save(requestSection);
        if(existingSection != null){
            requestSection.validMiddleSection(existingSection);
            addMiddleSection(line, requestSection, existingSection);
            return;
        }

        existingSection = sectionRepository.findByUpStation(requestSection.getDownStation()).orElse(null);
        if(existingSection != null) {
            addFirstSection(line, requestSection, existingSection);
            return;
        }

        Section prevSection = sectionRepository.findByDownStation(requestSection.getUpStation()).orElse(null);
        if(prevSection != null){
            prevSection.changeNextSection(requestSection);
        }
        line.addEndSection(requestSection);
    }

    public void addMiddleSection(Line line, Section requestSection, Section existingSection) {
        requestSection = sectionRepository.save(requestSection); //A-B 구간

        //기존 구간 역과 요청 구간 역으로 신규 구간 생성
        Section newSection = line.createNewSection(existingSection, requestSection); //B-C 구간
        newSection = sectionRepository.save(newSection);

        //요청 구간의 nextStationId에 신규 구간의 id로 셋팅
        requestSection.changeNextSection(newSection);

        //기존 구간의 이전 구간에 nextStationId 셋팅
        Section prevSection = sectionRepository.findByDownStation(requestSection.getUpStation()).orElse(null);
        if(prevSection != null) {
            prevSection.changeNextSection(requestSection);
        }

        //기존 구간 삭제
        line.removeSection(existingSection);

        //신규 구간 추가
        line.addMiddleSection(newSection);
        line.addMiddleSection(requestSection);
    }

    private void addFirstSection(Line line, Section requestSection, Section existingSection) {
        requestSection = sectionRepository.save(requestSection);

        //요청 구간의 nextStationId에 기존 구간의 id로 셋팅
        requestSection.changeNextSection(existingSection);

        //신규 구간 추가
        line.addMiddleSection(requestSection);
    }

    public void deleteSection(Line line, Station deleteStation) {
        Section leftStationSection = sectionRepository.findByDownStation(deleteStation).orElse(null); //A-B구간
        Section rightStationSection = sectionRepository.findByUpStation(deleteStation).orElse(null); //B-C구간

        if(rightStationSection != null) { //삭제역 기준 오른쪽 구간이 존재할 때 (마지막 구간이 아닐 때)
            //B-C구간의 상행역을 A로 변경, 구간 거리 수정
            rightStationSection.changeUpStationAndDistance(leftStationSection);

            //A-B구간 삭제
            line.removeSection(leftStationSection);

            //leftStationSection의 이전 구간 nextSectionId를 rightStationSection Id로 변경
            Section prevSection = sectionRepository.findByDownStation(leftStationSection.getUpStation()).orElse(null);
            if(prevSection != null) {
                prevSection.changeNextSection(rightStationSection);
            }
        }
        else {
            line.deleteDownSection(deleteStation);
        }

    }
}
