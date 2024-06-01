package models;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;


@XmlRootElement(name = "film")
public class Film {
	public Film(int id, String title, int year, String director, String stars, String review) {
		super();
		this.id = id;
		this.title = title;
		this.year = year;
		this.director = director;
		this.stars = stars;
		this.review = review;
	}

	public Film(String title, int year, String director, String stars, String review) {
		super();
		this.title = title;
		this.year = year;
		this.director = director;
		this.stars = stars;
		this.review = review;
	}

//	public Film(String title, int year, String director, String stars, String review, Timestamp added) {
//		super();
//		this.title = title;
//		this.year = year;
//		this.director = director;
//		this.stars = stars;
//		this.review = review;
//		this.added = added;
//	}

	public Film(int id, String title, int year, String director, String stars, String review, Timestamp added,
			Timestamp lastModified) {
		super();
		this.id = id;
		this.title = title;
		this.year = year;
		this.director = director;
		this.stars = stars;
		this.review = review;
		this.added = added;
		this.lastModified = lastModified;
	}

	public Film() {
		super();
	}

	int id;
	String title;
	int year;
	String director;
	String stars;
	String review;
	Timestamp added;
	Timestamp lastModified;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getStars() {
		return stars;
	}

	public void setStars(String stars) {
		this.stars = stars;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public Timestamp getAdded() {
//	public XMLGregorianCalendar getAdded() {
		return added;
	}

	public void setAdded(Timestamp added) {
		this.added = added;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return " id= " + id + "**\n title= " + title + "**\n year= " + year + "**\n director= " + director + "**\n stars= " + stars
				+ "**\n review= " + review + "**\n added= " + added + "**\n lastModified= " + lastModified + "**\n";
	}
}
