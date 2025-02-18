package kz.alfabank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;

/**
 * Created by u5442 on 16.10.2017.
 */
@Repository
public class PostGreeAdapter {
    final Logger logger = LoggerFactory.getLogger(PostGreeAdapter.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String exec(String functionName, String str) {
        String upperCased = "";
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            CallableStatement proc = conn.prepareCall("{ ? = call " + functionName + "(?) }");
            proc.registerOutParameter(1, Types.VARCHAR);
            proc.setString(2, str);
            logger.info("PostGress>>>" + proc.toString());
            proc.execute();
            upperCased = proc.getString(1);
            logger.info("PostGress<<<" + upperCased);
            proc.close();
            conn.close();
        } catch (Exception e) {
            logger.info(">>>>PostGress Error="+e.getMessage());
            return "{ \"status\":-1, \"text\":\" "+e.getMessage()+" \" }";
        }
        return upperCased;
    }

    public String writeLog(String idn, String operation, String errorCode, String errorMessage, String xData) {
        String strRes="";
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()){
            CallableStatement proc = conn.prepareCall("{ ? = call _logs(?,?,?,?,?) }");
            proc.registerOutParameter(1, Types.VARCHAR);
            proc.setString(2, idn);
            proc.setString(3, operation);
            proc.setString(4, errorCode);
            proc.setString(5, errorMessage);
            proc.setString(6, xData);
            logger.info("PostGress>>>" + proc.toString());
            proc.execute();
            conn.commit();
            strRes = proc.getString(1);
            logger.info("PostGress<<<" + strRes);
            proc.close();
            conn.close();
        }
        catch (Exception e)
        {
            logger.info(">>>>PostGress Error="+e.getMessage());
            return "{ \"status\":-1, \"text\":\" "+e.getMessage()+" \" }";
        }

        return strRes;
    }
}
