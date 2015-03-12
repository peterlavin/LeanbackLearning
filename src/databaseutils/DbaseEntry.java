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
//		dataForDB.add(strInitDetail);	3
//		dataForDB.add(strOutputLang);	4

		/*
		 * Get the server OS from System
		 */
		String server_os = System.getProperty("os.name");

		String insertStm = "INSERT INTO tbl_jobs SET "

		+ "userid='" + data.get(0)
		
		+ "',username='" + data.get(1)
		
		+ "',server_os='" + server_os
		
		+ "',topics='" + data.get(2)

		+ "',init_detail='" + data.get(3) 
		
		+ "',outputlanguage='" + data.get(4)
		
		+ "';";
		
		println(insertStm);

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
					println("returnedInt is " + returnedInt + " after initial DB entry creation.");
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
	 * Method to update a job with the users final 'level of detail' and time preferences
	 */
	public void updateFinalUserPrefs(Connection conn, String jobId, String final_detail, String time) {

		String updateSscStm = "UPDATE tbl_jobs SET final_detail=" + final_detail + ",time=" + time + " where jobid=" + jobId + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();
			stm.executeUpdate(updateSscStm);

		} catch (SQLException e) {
			System.err.println("ERROR in SSC UPDATE: " + e.getMessage());
		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * Method to update an existing job on its status after a call has been made
	 * to the remote Search-Summarise-Combine web service.
	 * 
	 * @param Connection, int, boolean
	 */
	public void updateSscStatus(Connection conn, int jobId,
			boolean successState, int xmlWordCount) {

		int boolToInt = 0;
		if (successState) {
			boolToInt = 1;
		} else {
			boolToInt = 0;
		}

		String updateSscStm = "UPDATE tbl_jobs SET ssc_status=" + boolToInt
				+ ",ssc_wc=" + xmlWordCount + " where jobid=" + jobId + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();
			stm.executeUpdate(updateSscStm);

		} catch (SQLException e) {
			System.err.println("ERROR in SSC UPDATE: " + e.getMessage());
		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * Method to update an existing job on its status after calling the Speech
	 * Systhesis service successfully.
	 * 
	 * @param int, Connection, boolean, String
	 */
	public void updateAudioFileStatus(int jobId, Connection conn,
			boolean ss_status, String fileName, int mp3Duration,
			int numberOfParts) {

		String ss_statusStr = "";

		if (ss_status) {
			ss_statusStr = "1";
		} else {
			ss_statusStr = "0";
		}

		String updateFileStm = "UPDATE tbl_jobs SET ss_status=" + ss_statusStr
				+ ",file_name='" + fileName + "',mp3_playtime=" + mp3Duration
				+ ",num_parts=" + numberOfParts + " where jobid=" + jobId + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();
			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {
			System.err.println("ERROR in file status UPDATE: " + e.getMessage()
					+ "\nSQL Error Code :" + e.getErrorCode());
		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * Updates the datebase where speech syntheses has failed and there is no
	 * details of the audio file to log.
	 * 
	 * @param int, Connection, boolean
	 */
	public void updateAudioFileStatus(int jobId2, Connection conn, int numberInPlaylist, boolean ss_status) {

		String ss_statusStr = "";

		if (ss_status) {
			ss_statusStr = "1";
		} else {
			ss_statusStr = "0";
		}

		String updateFileStm = "UPDATE tbl_jobs SET ss_status=" + ss_statusStr + ",num_parts=" + numberInPlaylist + " where jobid=" + jobId + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();
			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {
			System.err.println("ERROR in file status UPDATE: " + e.getMessage()
					+ "\nSQL Error Code :" + e.getErrorCode());
		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * Method to update database with Speech Syn details
	 * 
	 * @param int, Connection, String, int
	 */
	public void updateSpSynStatus(Connection conn, int jobId, String ssTimetaken, int fileLength) {

		String updateFileStm = "UPDATE tbl_jobs SET ss_filelength='" + fileLength
				+ "',ss_timetaken ='" + ssTimetaken
				+ "',multipart_job='1'"
				+ " where jobid=" + jobId + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();

			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {

			System.err
					.println("ERROR in Speech Syn UPDATE in DbaseEntry class, updateSpSynStatus method (tbl_jobs entry): "
							+ e.getMessage() + "\nSQL Error Code :"
							+ e.getErrorCode());

		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * Method to update the database with SS&C details.
	 * 
	 * @param int, Connection, String, int
	 */
	public void updateSearchAndCombStatus(int jobId, Connection conn,
			String timeTaken, int fileLength) {

		String updateFileStm = "UPDATE tbl_jobs SET ssc_timetaken=" + timeTaken
				+ ",ssc_filelength='" + fileLength + "'" + " where jobid="
				+ jobId + ";";

		Statement stm = null;
		try {

			stm = conn.createStatement();

			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {
			System.err.println("ERROR in SS&C UPDATE in DbaseEntry class: "
					+ e.getMessage() + "\nSQL Error Code :" + e.getErrorCode());
		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	
	/*
	 * Method to insert details for the FIRST part of a job in tbl_parts where it has
	 * been SUCCESSFUL
	 */
	public void updateSpSynStatusFirstPart(int jobId, Connection conn,
			int partNumber, int blockSize, int initialSet, int numSentences, int partWc,
			String ssTimeTaken, boolean ssStatus, int mp3PlayTime) {

		int boolToInt = 0;
		if (ssStatus) {
			boolToInt = 1;
		} else {
			boolToInt = 0;
		}

		String updateFileStm = "INSERT INTO tbl_parts SET " + "jobid=" + jobId
				+ ",part_num=" + partNumber + ",blocksize=" + blockSize
				+ ",initialset=" + initialSet + ",num_sents= " + numSentences
				+ ",part_wc= " + partWc + ",ss_timetaken='" + ssTimeTaken
				+ "',ss_status=" + boolToInt + ",mp3_playtime=" + mp3PlayTime + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();

			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {

			System.err
					.println("ERROR in Speech Syn UPDATE in DbaseEntry class, updateSpSynStatusFirstPart method (tbl_parts entry, Success ss): Part "
							+ partNumber + ", "
							+ e.getMessage() + "\nSQL Error Code :"
							+ e.getErrorCode());

		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	
	/*
	 * Method to insert details for all subsequent parts of a job in tbl_parts where it has
	 * been SUCCESSFUL
	 */
	public void updateSpSynStatusPart(int jobId, Connection conn,
			int partNumber, int blockSize, int initialSet, int numSentences, int partWc,
			String ssTimeTaken, boolean ssStatus, int mp3PlayTime) {

		int boolToInt = 0;
		if (ssStatus) {
			boolToInt = 1;
		} else {
			boolToInt = 0;
		}

		String updateFileStm = "INSERT INTO tbl_parts SET " + "jobid=" + jobId
				+ ",part_num=" + partNumber + ",blocksize=" + blockSize
				+ ",initialset=" + initialSet + ",num_sents= " + numSentences
				+ ",part_wc= " + partWc + ",ss_timetaken='" + ssTimeTaken
				+ "',ss_status=" + boolToInt + ",mp3_playtime=" + mp3PlayTime + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();

			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {

			System.err
					.println("ERROR in Speech Syn UPDATE in DbaseEntry class, updateSpSynStatusPart method (tbl_parts entry, Success ss): Part "
							+ partNumber + ", "
							+ e.getMessage() + "\nSQL Error Code :"
							+ e.getErrorCode());

		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			/*
			 * Connection is closed in the main Servlet, in the 
			 * processRemainderOfXML() method.
			 */
		}

	}
	
	/*
	 * Method to insert details for the FIRST part of a job in tbl_parts where it has
	 * FAILED
	 */
	public void updateSpSynStatusFirstPart(int jobId, Connection conn,
			int partNumber, int blockSize, int initialSet, int numSentences, int partWc, String ssTimeTaken,
			boolean ssStatus) {

		int boolToInt = 0;
		if (ssStatus) {
			boolToInt = 1;
		} else {
			boolToInt = 0;
		}

		String updateFileStm = "INSERT INTO tbl_parts SET " + "jobid=" + jobId
				+ ",part_num=" + partNumber + ",blocksize=" + blockSize
				+ ",initialset=" + initialSet
				+ ",num_sents= " + numSentences
				+ ",part_wc= " + partWc
				+ ",ss_timetaken='" + ssTimeTaken
				+ "',ss_status=" + boolToInt + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();

			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {

			System.err
					.println("ERROR in Speech Syn UPDATE in DbaseEntry class, updateSpSynStatusFirstPart method (tbl_parts entry, Failed ss): Part "
							+ partNumber + ", "
							+ e.getMessage() + "\nSQL Error Code :"
							+ e.getErrorCode());

		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	

	/*
	 * Method to insert details for all subsequent parts of a job in tbl_parts where it has
	 * FAILED
	 */
	public void updateSpSynStatusPart(int jobId, Connection conn,
			int partNumber, int blockSize, int initialSet, int numSentences, int partWc, String ssTimeTaken,
			boolean ssStatus) {

		int boolToInt = 0;
		if (ssStatus) {
			boolToInt = 1;
		} else {
			boolToInt = 0;
		}

		String updateFileStm = "INSERT INTO tbl_parts SET " + "jobid=" + jobId
				+ ",part_num=" + partNumber + ",blocksize=" + blockSize
				+ ",initialset=" + initialSet
				+ ",num_sents= " + numSentences
				+ ",part_wc= " + partWc
				+ ",ss_timetaken='" + ssTimeTaken
				+ "',ss_status=" + boolToInt + ";";

		Statement stm = null;
		
		try {

			stm = conn.createStatement();

			stm.executeUpdate(updateFileStm);

		} catch (SQLException e) {

			System.err
					.println("ERROR in Speech Syn UPDATE in DbaseEntry class, updateSpSynStatusPart method (tbl_parts entry, Failed ss): Part "
							+ partNumber + ", "
							+ e.getMessage() + "\nSQL Error Code :"
							+ e.getErrorCode());

		} finally {
			try {
				if(stm != null) {
					stm.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
//			if (conn != null){
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	

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
