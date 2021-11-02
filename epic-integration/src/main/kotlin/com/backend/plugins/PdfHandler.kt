package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.hl7.fhir.r4.model.Binary
import org.hl7.fhir.r4.model.Reference
import java.io.File

class PdfHandler(server: String = "public") {

    //the base of the fhir server
    private val baseURL: String = when (server) {
        "public" -> "http://hapi.fhir.org/baseR4"
        "local" -> "http://localhost:8000/fhir"
        else -> throw IllegalArgumentException("server parameter must be either \"public\" or \"local\"")
    }

    private val ctx: FhirContext = FhirContext.forR4()
    private val client = HttpClient()
    val jsonParser: IParser = ctx.newJsonParser()

    private var saveLocation: String = "src/main/resources/"
    private val font = PDType1Font.COURIER
    private val boldFont = PDType1Font.COURIER_BOLD
    private val fontSize: Float = 8F

    /**
     * Generates a new PDF if it doesn't exist and writes to it
     * @param header header for the text
     * @param text A String of text that can contain newlines
     * @param fileName name of the pdf file. Has to end in ".pdf"
     * @return returns Unit
     */
    suspend fun writeToPdf(header: String = "", text: String, fileName: String) {
        val file = File(saveLocation+fileName)

        if (file.exists()){
            val document = PDDocument.load(file)
            write(document, fileName, header, text)
        } else {
            val document = PDDocument()
            write(document, fileName, header, text)
        }
        // Also posting FHIR Binary
        createBinary(file, fileName.substringBefore("."))
    }

    /**
     * Private function that writes to given document
     * @param document the document instance of PDDocument
     * @param fileName name of the file ending in ".pdf"
     * @param header header for the text
     * @param text text to be written to pdf
     * @return returns Unit
     */
    private fun write(document: PDDocument, fileName: String, header: String = "", text: String){
        //write title here with an if statement
        var lines = 0
        var page = PDPage()
        document.addPage(page)
        var contentStream = PDPageContentStream(document, page)
        val textList = cleanText(text)
        contentStream.setLeading(12.0)
        contentStream.beginText()
        contentStream.newLineAtOffset(25F, 750F);
        contentStream.setFont(font, fontSize)
        if (header != ""){
            val headerList = cleanText(header)
            contentStream.setLeading(12.0)
            contentStream.setFont(boldFont, fontSize)
            headerList.forEach { it ->
                if (lines > 60){
                    lines = 0
                    //close curren contentStream
                    contentStream.endText()
                    contentStream.close()
                    //adding new page
                    page = PDPage()
                    document.addPage(page)
                    //setting up new contentStream
                    contentStream = PDPageContentStream(document, page)
                    contentStream.setFont(boldFont, fontSize)
                    contentStream.setLeading(12.0)
                    contentStream.beginText()
                    contentStream.newLineAtOffset(25F, 750F);
                }
                if (it.contains("\\n")){
                    contentStream.newLine()
                } else {
                    contentStream.showText(it)
                    contentStream.newLine()
                    lines++
                }

            }
            contentStream.setFont(font, fontSize)
            contentStream.newLine()
            contentStream.newLine()
            lines += 2
        }
        textList.forEach { it ->
            if (lines > 60){
                lines = 0
                //close curren contentStream
                contentStream.endText()
                contentStream.close()
                //adding new page
                page = PDPage()
                document.addPage(page)
                //setting up new contentStream
                contentStream = PDPageContentStream(document, page)
                contentStream.setFont(font, fontSize)
                contentStream.setLeading(12.0)
                contentStream.beginText()
                contentStream.newLineAtOffset(25F, 750F);
            }
            if (it.contains("\\n")){
                contentStream.newLine()
            } else {
                contentStream.showText(it)
                contentStream.newLine()
                lines++
            }
        }
        contentStream.endText()
        contentStream.close()
        document.save(saveLocation+fileName)
        document.close()
    }

    /**
     * cleans text for putting in pdf. Splits on new lines,
     * and wrap each line, so it does not go outside the pdf.
     * Using greedy algorithm to wrap the lines
     * @param text String to be cleaned
     * @return returns list of strings, each representing a line in the pdf
     */
    private fun cleanText(text: String): MutableList<String>{
        //remove newline
        var listOfStrings = text.split("\n").toMutableList()
        for (i in 0 until listOfStrings.size){
            if (listOfStrings[i] == ""){
                listOfStrings[i] = "\\n"
            }
        }
        var wrappedListOfString = mutableListOf<String>()
        for (i in 0 until listOfStrings.size){
            val currentLine = listOfStrings[i]
            var s = ""
            //greedy line wrapping
            for (j in currentLine.split(" ")){
                if ((s + j).length < 115){
                    s = s+" "+ j
                } else {
                    wrappedListOfString.add(s)
                    s = j
                }
            }
            wrappedListOfString.add(s)
        }

        return wrappedListOfString
    }

    private suspend fun createBinary(file: File, patientId: String) : HttpResponse {

        val binary = Binary()

        binary.data = file.readBytes()
        binary.contentType = ContentType.Application.Pdf.toString()
        binary.securityContext = Reference().setReference("Patient/${patientCommunication.parseBundleXMLToPatient(patientCommunication.patientSearch(identifier = patientId), isXML = false)?.id?.split("/")?.get(5)}")

        // post the Binary to the server
        val response: HttpResponse = client.post("$baseURL/Binary"){
            contentType(ContentType.Application.Json)
            body = jsonParser.encodeResourceToString(binary)
        }

        return response
    }
}
