/**
 * Created by kmofokeng on 01/11/2017.
 */

import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HTMLParser {

    private Document doc;
    private final File file = new File("./TechStack.JSON");
    private final List<String> headings = new ArrayList<String>();
    private final List<String> sections = Arrays.asList("Programming Stack", "Build Stack", "Infrastructure", "Monitoring");
    private String jsonString;
    private final int sectionCounter = sections.size();
    private int index;
    private Elements rows;

    public static void main(String[] args) throws IOException {
        HTMLParser parser = new HTMLParser();
        String techStackLink = "https://github.com/egis/handbook/blob/master/Tech-Stack.md";
        String techStackHTML = Jsoup.connect(techStackLink).get().html();
        FileWriter fw = new FileWriter(parser.file);
        BufferedWriter bw = new BufferedWriter(fw);
        parser.doc = Jsoup.parse(techStackHTML);
        Elements tables = parser.doc.select("table");
        parser.updateOutput(bw, tables, parser.sectionCounter);
        parser.displayTechStack();
        parser.close(bw);
    }

    private void updateOutput(BufferedWriter bw, Elements tables, int sectionCounter) {
        JsonObject jsonParentObject;
        int tableCounter = tables.size();
        for (Element table : tables) {
            for (Element row : table.select("tr")) {
                JsonObject jsonObject = new JsonObject();
                jsonParentObject = new JsonObject();
                getRows(row);
                calculateDifference(tableCounter, sectionCounter);
                printSections(jsonParentObject, bw, jsonObject, rows);
            }
            headings.clear();
            tableCounter--;
        }
    }

    private void getRows(Element row) {
        rows = row.cssSelector().contains("thead") ? row.select("th") : row.select("td");
    }

    private void calculateDifference(int tableCounter, int sectionCounter) {
        int difference = sectionCounter - tableCounter;
        switch (difference) {
            case 0:
                index = 0;
                break;
            case 1:
                index = 1;
                break;
            case 2:
                index = 2;
                break;
            case 3:
                index = 3;
                break;
        }
    }

    private void printSections(JsonObject jsonParentObject, BufferedWriter bw, JsonObject jsonObject, Elements rows) {
        if (rows.get(0).tagName().equals("th")) {
            for (int i = 0; i <= rows.size() - 1; i++) {
                headings.add(rows.get(i).text());
            }
        } else {
            for (int j = 0; j <= rows.size() - 1; j++) {
                jsonObject.addProperty(headings.get(j), rows.get(j).text());
                jsonParentObject.add(sections.get(index), jsonObject);
            }
            jsonString += jsonParentObject.toString();
            writeToFile(jsonParentObject, bw);
        }
    }

    private void displayTechStack() {
        System.out.println(jsonString);
    }

    private void writeToFile(JsonObject jsonParentObject, BufferedWriter bw) {
        try {
            bw.write(jsonParentObject.toString());
            bw.newLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void close(BufferedWriter bw) {
        try {
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}