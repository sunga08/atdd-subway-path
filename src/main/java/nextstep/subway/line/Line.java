package nextstep.subway.line;

import nextstep.subway.section.Section;
import nextstep.subway.section.Sections;
import nextstep.subway.station.Station;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
public class Line {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String color;

    @Embedded
    private Sections sections = new Sections();

    public Line() {}

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Line(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<Section> getSectionList() {
        return sections.getSections();
    }

    public List<Station> getStations() {
        return sections.getStations();
    }

    public void changeLineInfo(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void addSection(Section requestSection) {
        if(sections.isFirstSection(requestSection)) {
            registerSection(requestSection);
            return;
        }

        if(sections.isMiddleSection(requestSection)) {
            registerSection(sections.returnNewSection(requestSection));
            registerSection(requestSection);
            return;
        }

        sections.validateEndSection(requestSection);
        registerSection(requestSection);
    }

    private void registerSection(Section newSection) {
        newSection.registerLine(this);
    }

    public void deleteSection(Station deleteStation) {
        if(sections.isLastSection(deleteStation)) {
            sections.deleteDownStation(deleteStation);
        }
        sections.deleteMiddleStation(deleteStation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(getId(), line.getId()) && Objects.equals(getName(), line.getName()) && Objects.equals(getColor(), line.getColor()) && Objects.equals(getSectionList(), line.getSectionList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getColor(), getSectionList());
    }
}
