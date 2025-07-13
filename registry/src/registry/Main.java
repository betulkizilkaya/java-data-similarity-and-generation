package registry;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.sql.*;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String registryPath = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion";//Windows'un çok temel ayarlarının (örneğin varsayılan programlar, kurulum yolu vs.) tutulduğu bir yerdir.
        String keyDirectory = "HKEY_LOCAL_MACHINE"; //Hangi ana anahtarın kullanılacağı (sabit)
        String Url = "jdbc:sqlite:registry.db";

        try (Connection conn = DriverManager.getConnection(Url)) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS Tablo3 (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "key_directory TEXT, " +
                    "key_name TEXT, " +
                    "name TEXT, " +
                    "type TEXT, " +
                    "data TEXT)");
        
            //key ve value olduğu için map kullandık.
            Map<String, Object> values = Advapi32Util.registryGetValues(WinReg.HKEY_LOCAL_MACHINE, registryPath);//registryGetValues: Belirtilen registry yolundaki tüm key-value çiftlerini getirir.

            String insertSQL = "INSERT INTO Tablo3 (key_directory, key_name, name, type, data) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String name = entry.getKey();//Registry değerinin ismini verir.
                Object value = entry.getValue();//O değerin gerçek içeriğini verir.
                String type = value.getClass().getSimpleName(); // örn: String, Integer

                pstmt.setString(1, keyDirectory);
                pstmt.setString(2, registryPath);
                pstmt.setString(3, name);
                pstmt.setString(4, type);
                pstmt.setString(5, value.toString());

                pstmt.executeUpdate();
            }

            System.out.println("Data was saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
