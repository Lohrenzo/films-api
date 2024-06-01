package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.thoughtworks.xstream.XStream;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
//import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import database.FilmDaoEnum;
import models.Film;
//import models.FilmListWrapper;

/**
 * Servlet implementation class FilmsServlet
 */
@WebServlet("/films")
public class FilmsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	FilmDaoEnum dao;

	public FilmsServlet() {
		dao = FilmDaoEnum.INSTANCE;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		Gson gson = new Gson();
		String searchStr = request.getParameter("searchStr");

		ArrayList<Film> allFilms = null;
		if (searchStr == null) {
			allFilms = dao.getAllFilms();
		} else {
			allFilms = dao.searchFilm(searchStr);
		}

		// Set CORS headers
		setCorsHeaders(response);
		
		// String format = response.getHeader("Accept");
		String format = request.getHeader("Accept");

		if (format.equals("application/json") || format == null || (!format.equals("application/json")
				&& !format.equals("application/xml") && !format.equals("text/plain"))) {
			response.setContentType("application/json");
			String json = gson.toJson(allFilms);
			pw.write(json);
		} else if (format.equals("application/xml")) {
			response.setContentType("application/xml");

			// I Used XStream because JAXB was messing up the date format when marshalling.
			XStream xstream = new XStream();
			xstream.alias("film", Film.class);
			String xml = xstream.toXML(allFilms);
			pw.write(xml);
//			try {
//				// JAXB requires a wrapper class for lists or a JAXBElement
//				JAXBContext jaxbContext = JAXBContext.newInstance(FilmListWrapper.class);
//				Marshaller marshaller = jaxbContext.createMarshaller();
//
//				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//				FilmListWrapper wrapper = new FilmListWrapper();
//				wrapper.setFilms(allFilms);
//
//				// Marshalling the wrapper object instead of the list directly
//				marshaller.marshal(wrapper, pw);
//			} catch (IOException e) {
//				throw new ServletException("Error marshalling films to XML", e);
//				e.printStackTrace();
//			}
		} else if (format.equals("text/plain")) {
			response.setContentType("text/plain");
			StringBuilder builder = new StringBuilder();
			for (Film film : allFilms) {
				builder.append(film.toString()).append("#\n");
			}
			pw.write(builder.toString());
		}
		pw.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Determine the Content-Type of the request
		String contentType = request.getContentType();
		Film f = null;
		
		// Set CORS Headers
		setCorsHeaders(response);

		// Handle JSON Content Type
		if ("application/json".equals(contentType)) {
			// Prepare to read the request body.
			StringBuilder sb = new StringBuilder();
			String line = null;

			// Try to read the body.
			try (BufferedReader reader = request.getReader()) {
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			}
			// Send an error message if the body is not read.
			catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Error reading request body");
				return;
			}

			// Convert the request body (JSON) into a Contact object
			Gson gson = new Gson();

			try {
				f = gson.fromJson(sb.toString(), Film.class);
			} catch (JsonSyntaxException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Error parsing JSON request");
				return;
			}
		}
		// Handle XML Content Type
		else if ("application/xml".equals(contentType)) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(Film.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				f = (Film) unmarshaller.unmarshal(request.getInputStream());
			} catch (JAXBException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Invalid XML format");
				return;
			}
		} else {
			// Unsupported Content-Type
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			response.getWriter().write("Unsupported Content-Type. Please use application/json or application/xml.");
			return;
		}

		// Prepare the response writer
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		// Validate the Film object
		if (f.getTitle() == null || f.getTitle().trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("Title is a required field and cannot be empty.");
			out.close();
			return;
		}

		if (String.valueOf(f.getYear()) == null || String.valueOf(f.getYear()).isEmpty() || f.getYear() <= 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("Year is a required field and cannot be zero.");
			out.close();
			return;
		}

		if (f.getDirector() == null || f.getDirector().trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("Director is a required field and cannot be empty.");
			out.close();
			return;
		}

		if (f.getStars() == null || f.getStars().trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("Stars is a required field and cannot be empty.");
			out.close();
			return;
		}

		if (f.getReview() == null || f.getReview().trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("Review is a required field and cannot be empty.");
			out.close();
			return;
		}

		f.setTitle(f.getTitle().toUpperCase());
		f.setDirector(f.getDirector().toUpperCase());
		f.setStars(f.getStars().toUpperCase());

		try {
			dao.insertFilm(f);
			response.setStatus(HttpServletResponse.SC_CREATED);
			out.write(f.getTitle() + " has been added successfully.");
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write("An error occurred while adding the film.");
		} finally {
			out.close();
		}
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Set CORS headers
		setCorsHeaders(response);

		PrintWriter out = response.getWriter();
	    response.setContentType("text/plain");

	    // Default to JSON; you could also reject unsupported types if needed
		String contentType = request.getContentType();
		if (!"application/json".equals(contentType) && !"application/xml".equals(contentType)) {
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			out.write("Unsupported Content-Type. Please use application/json or application/xml.");
			return;
		}

		Film f;
		try {
			f = parseFilm(request, contentType);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write(e.getMessage());
			return;
		}

		if (f == null || String.valueOf(f.getId()) == null || f.getId() == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			Film existingFilm = dao.getFilmByID(f.getId());
			if (existingFilm == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				out.write("Film not found.");
				return;
			}

			updateExistingFilmWithNewValues(existingFilm, f);
			boolean updateResult = dao.updateFilm(existingFilm);
			if (updateResult) {
				response.setStatus(HttpServletResponse.SC_OK);
				out.write(existingFilm.getTitle() + " Updated Successfully.");
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				out.write("Failed to update film.");
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write("An error occurred during update.");
		} finally {
			out.close();
		}
	}

	private Film parseFilm(HttpServletRequest request, String contentType) throws Exception {
		if ("application/json".equals(contentType)) {
			return new Gson().fromJson(request.getReader(), Film.class);
		} else if ("application/xml".equals(contentType)) {
			JAXBContext jaxbContext = JAXBContext.newInstance(Film.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			return (Film) jaxbUnmarshaller.unmarshal(request.getInputStream());
		} else {
			throw new Exception("Unsupported content type: " + contentType);
		}
	}

	private void updateExistingFilmWithNewValues(Film existingFilm, Film newValues) {
		// Since all fields are optional, update only if a new value is present
		if (newValues.getTitle() != null && !newValues.getTitle().trim().isEmpty())
			existingFilm.setTitle(newValues.getTitle().toUpperCase());
		if (newValues.getYear() != 0
				|| String.valueOf(newValues.getYear()) != null && !String.valueOf(newValues.getYear()).trim().isEmpty())
			existingFilm.setYear(newValues.getYear());
		if (newValues.getDirector() != null && !newValues.getDirector().trim().isEmpty())
			existingFilm.setDirector(newValues.getDirector().toUpperCase());
		if (newValues.getStars() != null && !newValues.getStars().trim().isEmpty())
			existingFilm.setStars(newValues.getStars().toUpperCase());
		if (newValues.getReview() != null && !newValues.getReview().trim().isEmpty())
			existingFilm.setReview(newValues.getReview());
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		setCorsHeaders(response);

		// Parse the film ID from the request
		String id_string = request.getParameter("id");
        if (id_string == null || id_string.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("No film ID is provided.");
            return;
        }

		int id;
		try {
			id = Integer.parseInt(id_string);
		} catch (NumberFormatException e) {
			// If the ID is not a valid integer, respond with a bad request status
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Invalid film ID provided.");
			return;
		}

		try {
			// Attempt to delete the film directly
			boolean isDeleted = dao.deleteFilm(id);

			if (!isDeleted) {
				// If deleteFilm returns false, it means the film was not found
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().write("Film to delete does not exist.");
			} else {
				// If the film was successfully deleted, respond accordingly
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write("Film with ID: " + id + " has been successfully deleted.");
			}
		} catch (SQLException e) {
			// Handle any SQL exceptions during the delete operation
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("An error occurred while deleting the film.");
		}
	}

	private void setCorsHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
	    response.setHeader("Access-Control-Allow-Origin", "*");
	    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
	    
	    // HTTP 1.1
	 	response.setHeader("Access-Control-Max-Age", "86400");
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		resp.setHeader("Access-Control-Max-Age", "86400");
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
