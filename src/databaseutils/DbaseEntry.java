package databaseutils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DbaseEntry {
	
	/*
	 * Utility class which has methods called from throughout the application to
	 * update the application database at various stages.
	 */

	private int jobId = -1;
	private boolean debug;

	public DbaseEntry(boolean debug) {
		// constructor
		this.debug = debug;
	}
	
	
	public int CreatInitialDbEntry(Connection conn, ArrayList<String> data) {

		/*
		 * Order of details added to this ArrayList in SequencerServlet class
		 */
//		dataForDB.add(strIDnum);		0
//		dataForDB.add(strName);			1
//		dataForDB.add(strTopics);		2
//		dataForDB.add(strOutputLang);	3
//		dataForDB.add(strDetail);		4

		/*
		 * Get the server OS from System
		 */
		String server_os = System.getProperty("os.name");

		String insertStm = "INSERT INTO tbl_jobs SET "

		+ "user='" + data.get(0)

		+ "',server_os='" + server_os

		+ "',topics='" + data.get(1)

		+ "',time=" + data.get(2)

		+ ",detail=" + data.get(3)

		+ ",languageofquery='" + data.get(4)

		+ "',outputlanguage='" + data.get(5)
		
		+ "',multipart_job=1"

		+ ";";

		/*
		 * Add all fields to the database, get the entry ID number and return.
		 */
		Statement stm = null;
		ResultSet rset = null;

		try {
			stm = conn.createStatement();
			
			
			
			
			
			
			/*
			 * Put a timer around this, i.e. if not returned in say 3 seconds
			 * return -1 to indicate failure.
			 */
			
			
			/*
			 * The integer 1 returned denotes a successful execution
			 */
			int returnedInt = stm.executeUpdate(insertStm);

			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			/*
			 * Get the unique, automaticlally incremented int number of the last
			 * record created on this connection, i.e. it is concurrent safe.
			 */
			if (returnedInt == 1) {
				
				if(this.debug){
					println("returnedInt is 1 after initial DB entry creation.");
				}

				String getIdStm = "SELECT LAST_INSERT_ID()";
				rset = stm.executeQuery(getIdStm);

				if (rset.next()) {
					jobId = (int) rset.getLong("last_insert_id()");
				}

			} else {
				System.out.println("\nReturned int was not 1\n");
			}

		} catch (SQLException e) {

			if (debug) {
				e.printStackTrace();
			}

			System.err.println("ERROR in initial record INSERT: "
					+ e.getMessage() + "\nSQL Error Code :" + e.getErrorCode());

			return jobId; // -1 is returned here

		} finally {
			if (rset != null){
				try {
					rset.close();
				} catch (SQLException ignore) {
				}
			}
			if (stm != null){
				
				try {
					stm.close();
				} catch (SQLException ignore) {
				}
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return jobId;

	} // end method
	

	/*
	 * Log printing method
	 */
	private static void println(Object... obj) {
		if (obj.length == 0) {
			System.out.println();
		} else {
			System.out.println(obj[0]);
		}
	}
	
	
}
