package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class HtmlToPdfV2
{
    public static String[] jsonProcessor (String path) throws Exception
    {

        ObjectMapper jsonMapper = new ObjectMapper();

        Map<String, Object> data = jsonMapper.readValue(new File(path), Map.class);

        Map<String, String> employer = (Map<String, String>) data.get("employer");
        String employerName = employer.get("name");
        String employerPin = employer.get("pin");

        Map<String, String> employee = (Map<String, String>) data.get("employee");
        String employeeMainName = employee.get("main_name");
        String employeeOtherName = employee.get("other_names");
        String employeePin = employee.get("pin");

        List<Map<String, Object>> monthlyData = (List<Map<String, Object>>) data.get("monthly_data");

        StringBuilder rows = new StringBuilder();

        String totalPay = String.valueOf(monthlyData.getLast().get("chargeable_pay"));
        String totalTax = String.valueOf(monthlyData.getLast().get("paye_tax"));

        System.out.println(totalPay + " " + totalTax);

        for (Map<String, Object> month : monthlyData)
        {
            rows.append("<tr>");
            month.entrySet().stream()
            .forEach(entry -> 
            {
                rows.append("<td>").append(entry.getValue()).append("</td>");
            });
            rows.append("</tr>");
        }

        String[] processedJsonData = {
            employerName,
            employerPin,
            employeeMainName,
            employeeOtherName,
            employeePin,
            rows.toString(),
            totalPay,
            totalTax
        };

        return processedJsonData;

    }

    public static String[] csvProcessor(String path)
    {
        StringBuilder rows = new StringBuilder();
        String[] lastColumn = null;

        try (BufferedReader csvReader = new BufferedReader(new FileReader(path)))
        {
            String line;

            while ((line = csvReader.readLine()) != null)
            {
                // line = line.replaceAll("(\\d),(\\d{3})", "$1$2");
                // String[] columns = line.split(",");
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                lastColumn = columns;

                rows.append("<tr>");

                for (String col : columns)
                {
                    
                    col = col.replaceAll("\"", "");
                    rows.append("<td>").append(col).append("</td>");
                }

                rows.append("</tr>");
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        String totalTax = lastColumn[1].replace("\"", "");
        String totalPay = lastColumn[(lastColumn.length - 1)].replace("\"", "");

        String[] csvData = {rows.toString(), totalPay, totalTax};
        return csvData;
    }

    public static String templateReader(String path) throws IOException
    {
        String template = new String(Files.readAllBytes(Paths.get(path)));
        return template;

    }

    public static void createPdf(String htmlString, String fileName)
    {
        try (OutputStream os = new FileOutputStream("out/" + fileName))
        {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode()
            .useDefaultPageSize(11.69f, 8.27f, PdfRendererBuilder.PageSizeUnits.INCHES)
            .defaultTextDirection(PdfRendererBuilder.TextDirection.LTR);
            
            builder.withHtmlContent(htmlString, null);
            builder.toStream(os);
            builder.run();   
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, Exception
    {
        String kraHtmlTemplate = templateReader("/front-end/html-foundation/pdfs-from-html/kra-pdf-template-v2.html");
        // String[] htmlTemplateData = csvProcessor("html_data.csv");
        // String htmlData = htmlTemplateData[0];
        // String htmlTotalPay = htmlTemplateData[1];
        // String htmlTotalTax = htmlTemplateData[2];

        String[] htmlTemplateData = jsonProcessor("p9_data.json");
        String employerName = htmlTemplateData[0];
        String employerPin = htmlTemplateData[1];
        String employeeMainName = htmlTemplateData[2];
        String employeeOtherName = htmlTemplateData[3];
        String employeePin = htmlTemplateData[4];
        String rows = htmlTemplateData[5];
        String totalPay = htmlTemplateData[6];
        String totalTax = htmlTemplateData[7];

        String filledHtml = kraHtmlTemplate.replace("{{employerName}}", employerName)
        .replace("{{employerPin}}", employerPin)
        .replace("{{employeeMainName}}", employeeMainName)
        .replace("{{employeeOtherName}}", employeeOtherName)
        .replace("{{employeePin}}", employeePin)
        .replace("{{rows}}", rows)
        .replace("{{totalPay}}", totalPay != null ? totalPay : "")
        .replace("{{totalTax}}", totalTax != null ? totalTax : "");

        // System.out.println(filledHtml);

        createPdf(filledHtml, "Kra_P9_v2.pdf");

        

    }
}
