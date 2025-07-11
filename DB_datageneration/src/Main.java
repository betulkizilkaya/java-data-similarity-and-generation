import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:basic_data.db";

        String sql = """
               CREATE TABLE IF NOT EXISTS Tablo1(
               ID INTEGER PRIMARY KEY,
               Tamsayi1 INTEGER,
               Tamsayi2 INTEGER,
               GaussianTamsayi1 INTEGER,
               GaussianReelsayi1 REAL,
               GaussianReelsayi2 REAL,
               GaussianDate1 INTEGER,
               GaussianDate2 TEXT,
               GaussianDate3 REAL,
               Binary1 TEXT);
               """;

        try (Connection conn = DriverManager.getConnection(url); //Veritabanına bağlantı açar.
             Statement stmt = conn.createStatement()) { //SQL komutlarını çalıştıracak bir nesne oluşturur.

            stmt.execute(sql); //Tablo oluşturma SQL komutunu çalıştırır.
            System.out.println("Table Created Successfully!");

        } catch (SQLException ex) { //Eğer veritabanına bağlanırken ya da SQL çalıştırılırken hata olursa burada yakalanır.
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
