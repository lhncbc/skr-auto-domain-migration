package gov.nih.nlm.semmed.model;

import gov.nih.nlm.semmed.util.Constants;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A list of predications. Contains methods for loading predications from a DB.
 *
 *
 * @author rodriguezal
 *
 */
public class SemMedDB implements Serializable {

	private static Log log = LogFactory.getLog(SemMedDB.class);

	private static final long serialVersionUID = 1L;

	//private static Set<Integer> existingDocuments;

	private static DataSource ds;
	private static Connection con;
	private static Statement s;
	// private static DataSource testds;
	// public DataSource ds;

	private static final String CONCEPT_QUERY_PREFIX1 = "SELECT c.PREFERRED_NAME FROM "+
												"CONCEPT as c WHERE c.CUI = ";
	private static final String CONCEPT_QUERY_PREFIX2 = "SELECT c.PREFERRED_NAME, cs.SEMTYPE FROM " +
	"CONCEPT as c, CONCEPT_SEMTYPE as cs WHERE c.CONCEPT_ID = cs.CONCEPT_ID and c.cui = ";

	static{
		try{
			ds = setupDataSource();
			con = ds.getConnection();
		}catch(Exception e){
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.out.println("************************************************************");
			System.err.println("Couldn't load database driver!!");
			e.printStackTrace();
		}
	}

	public static DataSource getDataSource(){
		return ds;
	}

	public static Connection getConnection(){
		try {
		if(con == null)
			con = ds.getConnection();
		} catch(Exception e) {e.printStackTrace(); }
		return con;
	}

	public static String getConceptInfo(String cui) throws SQLException{
		String pname = null;
		try {
				if (cui!=null){
					if(con == null)
						con = ds.getConnection();
					if(s == null)
						s = con.createStatement();
					String query = new String(CONCEPT_QUERY_PREFIX1 + "'" + cui + "'");
						// log.debug(query);
					ResultSet rs = s.executeQuery(query);
					while(rs.next()) {
							pname = (String) rs.getString(1);
							// log.debug("result name = " + pname);
					}
					rs.close();
				}
		} catch(SQLException e) {throw e;}
		return pname;
	}

	public static CUIInfo getConceptSemtypeInfo(String cui) throws SQLException{
		CUIInfo info = new CUIInfo();
		try {
				if (cui!=null){
					if(con == null)
						con = ds.getConnection();
					if(s == null)
						s = con.createStatement();
					String query = new String(CONCEPT_QUERY_PREFIX2 + "'" + cui + "'");
						// log.debug(query);
					ResultSet rs = s.executeQuery(query);
						int i = 0;
						while(rs.next()) {
							if(i==0) {
								info.pname = new String(rs.getString(1));
								info.stype = new ArrayList<String>();
								info.stype.add(rs.getString(2));
								// log.debug("result pname = " + info.pname + ": " + rs.getString(2));
								// log.debug("list length = " + info.stype.size());
							} else {
								info.stype.add(rs.getString(2));
								// log.debug("result pname = " + info.pname + ": " + rs.getString(2));
								// log.debug("list length = " + info.stype.size());
							}
							i++;
						}
						// log.debug("result CUI = " + cui);
						rs.close();
				}
		} catch(SQLException e) {throw e;}
		return info;
	}




	protected static DataSource setupDataSource() throws Exception{
		Context ctx = new InitialContext();
        DataSource ds =
            (DataSource)ctx.lookup("java:comp/env/jdbc/SemMedDB");
      return ds;
	}


}
