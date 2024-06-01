package controllers;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;

import database.FilmDaoEnum;
import models.Film;

/**
 * Servlet implementation class FilmByIdServlet
 */
@WebServlet("/film")
public class FilmByIdServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	FilmDaoEnum dao;

	public FilmByIdServlet() {
		dao = FilmDaoEnum.INSTANCE;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set CORS headers
		setCorsHeaders(response);

		PrintWriter pw = response.getWriter();
		String sid = request.getParameter("id");

		int id;
        try {
            id = Integer.parseInt(sid);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid film ID.");
            return;
        }

        Film film;
        film = dao.getFilmByID(id);
		if (film == null) {
		    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		    response.getWriter().write("Film with ID: " + id + " does not exist.");
		    return;
		}
		
		String format = request.getHeader("Accept");
		
		if (format.equals("application/json") || format == null || (!format.equals("application/json")
				&& !format.equals("application/xml") && !format.equals("text/plain"))) {
            response.setContentType("application/json");
            Gson gson = new Gson();
            String json = gson.toJson(film);
            response.setStatus(HttpServletResponse.SC_OK);
            pw.write(json);
        } else if (format.equals("application/xml")) {
            response.setContentType("application/xml");
            XStream xstream = new XStream();
            xstream.alias("film", Film.class);
            String xml = xstream.toXML(film);
            response.setStatus(HttpServletResponse.SC_OK);
            pw.write(xml);
        } else if (format.equals("text/plain")) {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            pw.write(film.toString());
        }

        pw.close();
	}
	
	private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
