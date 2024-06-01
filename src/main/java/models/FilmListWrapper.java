package models;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "films")
public class FilmListWrapper {
	private List<Film> films;
	
	@XmlElement(name = "film")
	public List<Film> getFilms() {
		return films;
	}
	
	public void setFilms(List<Film> films) {
        this.films = films;
    }
}
