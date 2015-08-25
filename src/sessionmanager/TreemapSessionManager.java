package sessionmanager;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class TreemapSessionManager
 */
@WebServlet(description = "Servlet which manages data stored in the HTTP session for Treemap visualisation", urlPatterns = { "/TreemapSessionManager" })
public class TreemapSessionManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TreemapSessionManager() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("add") != null) {
					
			Map<String, String[]> map = request.getParameterMap();
		
						
			for (String key : map.keySet()) {
				
				System.out.println("Activity is: " + key);
								
			}
			
			
			for (String[] value : map.values()) {
				
				for (String str : value){
					System.out.print(str);
				}
								
			}
		
			System.out.println();
		
		}
		
		//System.out.println("\ndoPut Method called: " + strTest);
		
		

        HttpSession session = request.getSession();
        
      //  String origSess = (String) session.getAttribute("user");
        
        //session.setAttribute("user", origSess + ", " + strTest);

        //setting session to expiry in 30 mins
        session.setMaxInactiveInterval(30*60);
        
     //   String newSess = (String) session.getAttribute("user");
        
//    	System.out.println("Session now is: " + newSess);
//    	System.out.println(session.getId());
    	
		
		/*
		 * Write something to response
		 */
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("");
		
	}

}
