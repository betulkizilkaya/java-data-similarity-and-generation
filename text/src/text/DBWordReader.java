package text;

import java.sql.*;
import java.util.*;

public class DBWordReader {
    public static List<String> getWords(Connection conn) throws SQLException {
        List<String> list = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT kelime FROM Tablo2");

        while (rs.next()) {
            list.add(rs.getString("kelime"));//Her satırdan kelime sütunu alınarak list listesine eklenir.
        }
        return list;
    }
}
