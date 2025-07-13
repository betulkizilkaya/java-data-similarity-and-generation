import java.sql.*;
import java.util.*;

public class AllColumns {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:basic_data.db";

        String[] columns = {
            "Tamsayi1", "Tamsayi2", "GaussianTamsayi1",
            "GaussianReelsayi1", "GaussianReelsayi2",
            "GaussianDate1", "GaussianDate3"
        };

        try (Connection conn = DriverManager.getConnection(url)) {
            
            createTable(conn);//tablo oluştur
            
            double totalSimilarity = 0.0;
            int validColumnCount = 0;

            for (String col : columns) {
                List<Double> data = getColumnData(conn, col);//Sütunun sayısal verileri alınır

                
                List<Double> x = new ArrayList<>();//Veriler ikili eşlenir
                List<Double> y = new ArrayList<>();

                for (int i = 0; i < data.size() - 1; i += 2) {
                    x.add(data.get(i));
                    y.add(data.get(i + 1));
                }

                double similarity = computePearson(x, y);//Benzerlik hesaplaması

                // Sonucu yazdırıp DB'ye kaydetme
                if (!Double.isNaN(similarity)) {
                    insertSelfSimilarity(conn, col, similarity);
                    System.out.printf("Self-pair similarity for %-20s: %.4f%n", col, similarity);
                    totalSimilarity += similarity;
                    validColumnCount++;
                } else {
                    System.out.printf("Skipped %-20s (not enough data or NaNs)%n", col);
                }
            }
            if (validColumnCount > 0) {
                double averageSimilarity = totalSimilarity / validColumnCount;
                System.out.printf("📊 Similarity average of all columns: %.4f%n", averageSimilarity);
                insertSelfSimilarity(conn, "AVERAGE", averageSimilarity);
            } else {
                System.out.println("Could not calculate valid similarity for any column!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Belirli bir sütundaki sayısal verileri getir
    public static List<Double> getColumnData(Connection conn, String columnName) throws SQLException {
        List<Double> values = new ArrayList<>();
        String sql = "SELECT " + columnName + " FROM Tablo1 WHERE " + columnName + " IS NOT NULL";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double val = rs.getDouble(1);
                if (!Double.isNaN(val) && !Double.isInfinite(val)) {//Sayıysa,NaN değilse,Sonsuzluk değilse -> values listesine eklenir.
                    values.add(val);
                }
            }
        }
        return values;
    }

    // Pearson korelasyon katsayısı hesapla
    public static double computePearson(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.size() < 2) return Double.NaN;

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
        if (denominator == 0 || Double.isNaN(denominator)) return Double.NaN;

        return numerator / denominator;
    }

    public static void createTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS AverageOfAll (
                ColumnName TEXT,
                Similarity REAL
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public static void insertSelfSimilarity(Connection conn, String col, double similarity) throws SQLException {
        String sql = "INSERT INTO AverageOfAll (ColumnName, Similarity) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, col);
            pstmt.setDouble(2, similarity);
            pstmt.executeUpdate();
        }
    }
}
