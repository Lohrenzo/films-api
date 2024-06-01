package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import models.Film;

public enum FilmDaoEnum {
	/**
	 * Singleton pattern.
	 */
	INSTANCE;

	String user = "fubarala";
	String password = "vestIkol9";
	String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/" + user;

	/**
	 * Loads the necessary JDBC driver and establishes a connection to the database.
	 *
	 * @return a Connection object representing the database connection
	 * @throws SQLException if a database access error occurs
	 */
	private Connection getConnection() throws SQLException {

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return DriverManager.getConnection(url, user, password);
	}


	/**
	 * This takes in resultset as a parameter, converts the entry to a Film object and returns the object.
	 * Constructs a Film object using the data from the specified ResultSet.
	 * 
	 * @param rs The ResultSet containing the film data retrieved from the database.
	 * @return Film object populated with data from the ResultSet.
	 */
	private Film getNextFilm(ResultSet rs) {
		Film film = null;
		try {
			film = new Film(rs.getInt("id"), rs.getString("title"), rs.getInt("year"), rs.getString("director"),
					rs.getString("stars"), rs.getString("review"), rs.getTimestamp("added"), rs.getTimestamp("last_modified"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return film;
	}

	/**
	 * Retrieves all films available in the database.
	 *
	 * @return ArrayList of Film objects representing all films in the database
	 */
	public ArrayList<Film> getAllFilms() {

		ArrayList<Film> allFilms = new ArrayList<Film>();
		String sql = "SELECT * FROM films;";

		try (Connection conn = this.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			// Retrieve the results
			while (rs.next()) {
				Film oneFilm = getNextFilm(rs);
				allFilms.add(oneFilm);
			}

			stmt.close();
		} catch (SQLException se) {
			System.out.println(se);
		}

		return allFilms;
	}

	/**
	 * Retrieves films from the database with pagination.
	 *
	 * @param limit  The maximum number of films to return
	 * @param offset The number of films to skip
	 * @return ArrayList of Film objects representing films within the specified limit and offset
	 */
	public ArrayList<Film> getAllFilmsPagination(int limit, int offset) {
		ArrayList<Film> allFilms = new ArrayList<>();
		String sql = "SELECT * FROM films LIMIT ? OFFSET ?;";

		try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, limit);
			pstmt.setInt(2, offset);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Film oneFilm = getNextFilm(rs);
					allFilms.add(oneFilm);
				}
			}
		} catch (SQLException se) {
			System.out.println(se);
		}

		return allFilms;
	}

	/**
	 * Searches for films in the database based on a search string.
	 *
	 * @param searchStr The search string used to query films by title, director, or stars
	 * @return ArrayList of Film objects representing the search results
	 */
	public ArrayList<Film> searchFilm(String searchStr) {
		ArrayList<Film> searchResult = new ArrayList<>();

		// SQL query checks for the title, director and stars columns
		String sql = "SELECT * FROM films WHERE LOWER(title) LIKE LOWER(?) OR LOWER(director) LIKE LOWER(?) OR LOWER(stars) LIKE LOWER(?);";

		try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// Prepare the search string to include the wildcard character before and after
			String searchWithWildcards = "%" + searchStr + "%";

			// Set the prepared statement's parameters for title, director, and stars
			pstmt.setString(1, searchWithWildcards);
			pstmt.setString(2, searchWithWildcards);
			pstmt.setString(3, searchWithWildcards);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Film film = getNextFilm(rs);
					searchResult.add(film);
				}
			}
		} catch (SQLException se) {
			System.out.println(se);
		}

		return searchResult;
	}

	/**
	 * Searches for films in the database with pagination based on a search string.
	 *
	 * @param searchStr The search string used to query films by title, director, or stars
	 * @param limit     The maximum number of films to return
	 * @param offset    The number of films to skip
	 * @return ArrayList of Film objects representing the search results within the specified limit and offset
	 */
	public ArrayList<Film> searchFilmPagination(String searchStr, int limit, int offset) {
		ArrayList<Film> searchResult = new ArrayList<>();
		// Update the SQL query to include LIMIT and OFFSET parameters
		String sql = "SELECT * FROM films WHERE LOWER(title) LIKE LOWER(?) OR LOWER(director) LIKE LOWER(?) OR LOWER(stars) LIKE LOWER(?) ORDER BY title ASC LIMIT ? OFFSET ?;";

		try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// Prepare the search string to include the wildcard character before and after
			String searchWithWildcards = "%" + searchStr + "%";

			// Set the prepared statement's parameters for title, director, stars, limit,
			// and offset
			pstmt.setString(1, searchWithWildcards);
			pstmt.setString(2, searchWithWildcards);
			pstmt.setString(3, searchWithWildcards);
			pstmt.setInt(4, limit); // Set limit
			pstmt.setInt(5, offset); // Set offset

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Film film = getNextFilm(rs);
					searchResult.add(film);
				}
			}
		} catch (SQLException se) {
			System.out.println(se);
		}

		return searchResult;
	}

	/**
	 * Retrieves a film from the database based on its ID.
	 *
	 * @param id The ID of the film to retrieve
	 * @return The Film object representing the film with the specified ID, or null if not found
	 */
	public Film getFilmByID(int id) {
		String sql = "SELECT * FROM films WHERE id = ?;";
		Film f = null;

		try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					f = getNextFilm(rs);
				}
			}
		} catch (SQLException se) {
			System.out.println(se);
		}

		return f;
	}

	/**
	 * Inserts a new film into the database.
	 *
	 * @param f The Film object representing the film to be inserted
	 * @return true if the film was successfully inserted, false otherwise
	 * @throws SQLException if an SQL exception occurs during insertion
	 */
	public boolean insertFilm(Film f) throws SQLException {
		String sql = "INSERT INTO films (title, year, director, stars, review, added, last_modified) VALUES (?, ?, ?, ?, ?, NOW(), NOW());";

		try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, f.getTitle());
			pstmt.setInt(2, f.getYear());
			pstmt.setString(3, f.getDirector());
			pstmt.setString(4, f.getStars());
			pstmt.setString(5, f.getReview());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				// Retrieve the generated id
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						f.setId(generatedKeys.getInt(1)); // Set the id back to the film object, if necessary
					} else {
						throw new SQLException("Creating film failed, no ID obtained.");
					}
				}
			}
			System.out.print("Successfully Added New Film");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Updates an existing film in the database.
	 *
	 * @param f The Film object representing the updated film information
	 * @return true if the film was successfully updated, false otherwise
	 * @throws SQLException if an SQL exception occurs during update
	 */
	public boolean updateFilm(Film f) throws SQLException {
		boolean b = false;
		String sql = "UPDATE films SET title = ?, year = ?, director = ?, stars = ?, review = ?, last_modified = NOW() WHERE id = ?;";
		try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, f.getTitle());
			pstmt.setInt(2, f.getYear());
			pstmt.setString(3, f.getDirector());
			pstmt.setString(4, f.getStars());
			pstmt.setString(5, f.getReview());
			pstmt.setInt(6, f.getId());

			int affectedRows = pstmt.executeUpdate();
			b = affectedRows > 0;
		} catch (SQLException e) {
			throw new SQLException("Could not update film", e);
		}
		System.out.print("Film Updated");
		return b;
	}

	/**
	 * Deletes a film from the database based on its ID.
	 *
	 * @param id The ID of the film to delete
	 * @return true if the film was successfully deleted, false otherwise
	 * @throws SQLException if an SQL exception occurs during deletion
	 */
	public boolean deleteFilm(int id) throws SQLException {
		boolean b = false;
		String sql = "DELETE FROM films WHERE id = ?;";
		try (Connection conn = (Connection) this.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, id);

			int affectedRows = pstmt.executeUpdate();
			b = affectedRows > 0;
		} catch (SQLException e) {
			throw new SQLException("Could not delete film", e);
		}
		return b;
	}
}
