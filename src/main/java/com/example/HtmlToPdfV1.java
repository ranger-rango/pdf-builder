package com.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class HtmlToPdfV1
{
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

    public static void main(String[] args) throws IOException
    {
        String kraHtmlTemplate = templateReader("src/main/resources/p-nine-report.html");
        String ukulimaHtmlTemplate = templateReader("src/main/resources/account-statements.html");

        createPdf(kraHtmlTemplate, "Sample BNG P9 Report.pdf");
        createPdf(ukulimaHtmlTemplate, "Sample Account Statements Report.pdf");
    }
}
