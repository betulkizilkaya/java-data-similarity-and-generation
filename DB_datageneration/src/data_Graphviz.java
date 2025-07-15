import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Factory.to;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;

public class data_Graphviz {
 public static void main(String[]args) {
	 System.out.println("The code is initialized.");
     Graphviz.useEngine(new GraphvizCmdLineEngine());//Graphviz'in komut satırı motorunu (dot.exe) kullanacağını belirtir.
     System.out.println("Dot engine selected.");
     
     MutableGraph graph = mutGraph("BinaryColumnSimilarity").setDirected(true);
     
     File dotOut = new File("BinaryColumnSimilarity.dot");
     
     try (
             Connection conn = DriverManager.getConnection("jdbc:sqlite:basic_data.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Column1, Column2, Similarity FROM BinaryColumnSimilarity");
         ) {
             System.out.println("✅ Database connection established.");

             boolean hasData = false;

             while (rs.next()) {
                 hasData = true;
                 String source = rs.getString("Column1");//Her kelime çifti için ok eklenir. 
                 String target = rs.getString("Column2");
                 double score = rs.getDouble("Similarity");//Label olarak skoru gösterir.

                 graph.add(mutNode(source).addLink(to(mutNode(target)).with(Label.of(String.format("%.10f", score)))));//Her kelime çifti için ok eklenir, Label skoru gösterir.
             }

             if (hasData) {
                 System.out.println("Writing dot file...");
                 FileWriter writer = new FileWriter(dotOut);//DOT dosyası diske yazılır.
                 writer.write(graph.toString());
                 writer.close();
                 System.out.println("Dot file written: " + dotOut.getAbsolutePath());

                 System.out.println("Creating PNG...");
                 File svgOut = new File("BinaryColumnSimilarity.png");

                 ProcessBuilder pb = new ProcessBuilder(
                     "dot", "-Tpng", dotOut.getAbsolutePath(), "-o", svgOut.getAbsolutePath()//dot -Tpng similarities.dot -o Similarities-graph.png
                 );
                 pb.inheritIO().start().waitFor();
                 //pb.inheritIO()->Terminalde dot komutunun çıktısını Java konsolunda görmesini sağlar.
                 //pb.start()->dot programı bu noktada çalışır ve PNG üretmeye başlar.
                 //.waitFor()->dot işlemi tamamlanana kadar bekler

                 System.out.println("✅ PNG successfully created: " + svgOut.getAbsolutePath());
             } else {
                 System.out.println("⚠️ There is no data in the database.");
             }
             

         } catch (Exception e) {
             e.printStackTrace(); // Hataları göster
         }
 }
}
