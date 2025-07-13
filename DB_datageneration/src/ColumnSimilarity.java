import java.sql.*;
import java.util.*;

public class ColumnSimilarity {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:basic_data.db";
        String[] columns = {
            "Tamsayi1", "Tamsayi2", "GaussianTamsayi1",
            "GaussianReelsayi1", "GaussianReelsayi2",
            "GaussianDate1", "GaussianDate3"
        };

        try (Connection conn = DriverManager.getConnection(url)) {
        	createSimilarityTable(conn);//ColumnSimilarity oluşur
        	
            for (String col : columns) {
                List<Double> data = getColumnData(conn, col);

                List<Double> original = new ArrayList<>();//veri çiftleri oluşturuluyor
                List<Double> lagged = new ArrayList<>();

                for (int i = 0; i < data.size() - 1; i++) {
                    original.add(data.get(i));
                    lagged.add(data.get(i + 1));
                }

                double autocorrelation = computePearson(original, lagged);//Pearson korelasyonu 
                System.out.printf("Autocorrelation (lag=1) for %-20s: %.4f%n", col, autocorrelation);
                insertSimilarity(conn, col, autocorrelation);//DB kayıt
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Double> getColumnData(Connection conn, String columnName) throws SQLException {
        List<Double> values = new ArrayList<>();
        String sql = "SELECT " + columnName + " FROM Tablo1 WHERE " + columnName + " IS NOT NULL";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double val = rs.getDouble(1);//Sorgu ile belirtilen sayılar alınır(null hariç)
                if (!Double.isNaN(val) && !Double.isInfinite(val)) {
                    values.add(val);
                }
            }
        }
        return values;
    }

    public static double computePearson(List<Double> x, List<Double> y) {//Pearson hesaplama
        if (x.size() != y.size() || x.size() < 2) return Double.NaN;//Aynı uzunlukta olması veya en az 2 değer içermesi gerekir. Şartlar sağlanmazsa NaN döner

        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0;
        double sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            double xi = x.get(i);
            double yi = y.get(i);
            sumX += xi;
            sumY += yi;
            sumXY += xi * yi;
            sumX2 += xi * xi;
            sumY2 += yi * yi;
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0 || Double.isNaN(denominator)) return Double.NaN;//denominator sıfırsa veya tanımsızsa -> NaN.
        return numerator / denominator;
    }
    
    public static void createSimilarityTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS ColumnSimilarity (
                Column1 TEXT,
                Similarity REAL
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    public static void insertSimilarity(Connection conn, String col,double similarity) throws SQLException {
        String insertSQL = "INSERT INTO ColumnSimilarity (Column1, Similarity) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, col);
            pstmt.setDouble(2, similarity);
            pstmt.executeUpdate();
        }
    }
}
