package com.backend.plugins

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.File

class PdfHandler {

    private var saveLocation: String = "src/main/resources/"


    /**
     * Generates a new PDF and writes to it
     * @param text A String of text that can contain newlines
     * @param documentName name of the document. Has to end in ".pdf"
     * @return returns Unit
     */
    fun writeToNewPdf(text: String, documentName: String) {
        val document = PDDocument()
        val page = PDPage()
        document.addPage(page)
        val contentStream = PDPageContentStream(document, page)
        contentStream.setFont(PDType1Font.COURIER, 12F)
        contentStream.setLeading(12.0)
        contentStream.beginText()
        contentStream.newLineAtOffset(25F, 700F);
        val lines = text.lines()
        lines.forEach { it ->
            contentStream.showText(it)
            contentStream.newLine()
        }
        contentStream.endText()
        contentStream.close()
        document.save(saveLocation+documentName)
        document.close()
    }

    /**
     *
     */
    fun addToPdf(text: String, documentName: String){
        val file = File(saveLocation+documentName)
        val document = PDDocument.load(file)
        PDPage
    }

}

fun main() {
    val text = """
        this is a text
        with new lines
        did this work?
    """.trimIndent()
    val test: PdfHandler = PdfHandler()
    test.writeToNewPdf(text, "testPDF.pdf")
}