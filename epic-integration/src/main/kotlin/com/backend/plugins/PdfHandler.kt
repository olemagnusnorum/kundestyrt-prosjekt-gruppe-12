package com.backend.plugins


import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream

import org.apache.pdfbox.pdmodel.font.PDFont
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
    fun writeToNewPdf(title: String = "", text: String, fileName: String) {
        val font = PDType1Font.COURIER
        val fontSize: Float = 8F

        var lines = 0

        val document = PDDocument()
        write(document, fileName, font, fontSize, text)
    }


    /**
     * Writes to existing pdf by adding new page to the pdf
     * @param text A String of text that can contain newlines
     * @param documentName name of the document. Has to end in ".pdf"
     * @return returns Unit
     */
    fun addToPdf(text: String, fileName: String){
        val font = PDType1Font.COURIER
        val fontSize: Float = 8F
        var lines = 0
        val file = File(saveLocation+fileName)
        val document = PDDocument.load(file)
        write(document, fileName, font, fontSize, text)
    }

    private fun write(document: PDDocument, fileName: String, font: PDFont, fontSize: Float, text: String){
        //write title here with an if statement
        var lines = 0
        var page = PDPage()
        document.addPage(page)
        var contentStream = PDPageContentStream(document, page)
        contentStream.setFont(font, fontSize)
        val textList = cleanText(text)
        contentStream.setLeading(12.0)
        contentStream.beginText()
        contentStream.newLineAtOffset(25F, 750F);

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


    private fun writeHeader(contentStream: PDPageContentStream, date: String){

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

}

fun main() {
    val lorem = """{
    "resourceType": "Bundle",
    "id": "19ebafae-6958-48ff-9435-815941a88436",
    "meta": {
        "lastUpdated": "2021-10-21T13:37:45.714+00:00"
    },
    "type": "searchset",
    "total": 1,
    "link": [
        {
            "relation": "self",
            "url": "http://localhost:8000/fhir/Condition?clinical-status=active&code=77386006"
        }
    ],
    "entry": [
        {
            "fullUrl": "http://localhost:8000/fhir/Condition/4",
            "resource": {
                "resourceType": "Condition",
                "id": "4",
                "meta": {
                    "versionId": "1",
                    "lastUpdated": "2021-10-21T08:02:36.678+00:00",
                    "source": "#a7L93FaTDiCSPhG0"
                },
                "clinicalStatus": {
                    "coding": [
                        {
                            "system": "http://terminology.hl7.org/CodeSystem/condition-clinical",
                            "code": "active",
                            "display": "Active"
                        }
                    ]
                },
                "verificationStatus": {
                    "coding": [
                        {
                            "system": "http://terminology.hl7.org/CodeSystem/condition-ver-status",
                            "code": "confirmed",
                            "display": "Confirmed"
                        }
                    ]
                },
                "category": [
                    {
                        "coding": [
                            {
                                "system": "http://terminology.hl7.org/CodeSystem/condition-category",
                                "code": "encounter-diagnosis",
                                "display": "Encounter diagnosis"
                            }
                        ]
                    }
                ],
                "severity": {
                    "coding": [
                        {
                            "system": "http://hl7.org/fhir/ValueSet/condition-severity",
                            "code": "255604002",
                            "display": "Mild"
                        }
                    ]
                },
                "code": {
                    "coding": [
                        {
                            "system": "urn:oid:2.16.840.1.113883.6.96",
                            "code": "77386006",
                            "display": "Pregnant"
                        }
                    ]
                },
                "subject": {
                    "reference": "Patient/2"
                },
                "onsetDateTime": "1999-06-02",
                "abatementDateTime": "2000-04-02",
                "note": [
                    {
                        "text": "GRAVID"
                    }
                ]
            },
            "search": {
                "mode": "match"
            }
        }
    ]
}
    """.trimIndent()
    val text = """
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse egestas odio id eleifend luctus. In dolor massa, scelerisque sed cursus at, congue vitae tellus. Ut nec tortor quam. Nulla rhoncus vitae ligula et aliquam. Donec pharetra felis faucibus, ornare sem ut, tristique quam. Donec mollis erat et neque vehicula vulputate. Aliquam erat volutpat. Nulla dui orci, ullamcorper a tellus ac, laoreet maximus leo. Duis egestas eget magna eu hendrerit. Vivamus sapien ligula, scelerisque eu metus vitae, congue molestie augue. Quisque ac tellus a nisi porttitor ultrices. Morbi tincidunt id massa quis dictum. Donec ultricies elit eget lorem varius, at tincidunt lacus vulputate. Donec tincidunt eget est id aliquam. Nullam tincidunt sagittis nunc nec efficitur.
        
Phasellus ipsum justo, posuere sed ultricies ut, tristique in odio. Pellentesque ac nibh ex. Nulla facilisi. Mauris condimentum aliquam vulputate. Cras ullamcorper eros at lectus molestie cursus. Fusce nec ultrices ipsum, sed cursus felis. Nunc ac vestibulum felis, eu porta risus. Sed accumsan a ligula tincidunt aliquet. Quisque lectus diam, aliquet vel arcu in, ornare cursus dui. Nulla sed turpis sem.Sed luctus dignissim ornare. Mauris eget lectus sed nunc aliquet malesuada. Etiam in libero dui. Morbi ac facilisis metus. Fusce vitae tempor sem. Praesent pretium metus lobortis lectus hendrerit imperdiet. Aenean interdum, urna id posuere sagittis, mi velit ultricies sapien, non pulvinar enim nisi vulputate turpis. Donec ipsum nulla, pellentesque ac nisl a, tempus commodo lorem. In malesuada tempor sodales. Curabitur eleifend ex eget nunc vehicula, vel imperdiet odio viverra. Etiam sodales vehicula ullamcorper. Cras elementum leo ut nunc dignissim aliquam.

Integer mattis pretium felis, vitae efficitur urna egestas id. Sed at cursus nisl, id condimentum lectus. Integer eleifend dapibus varius. In porta congue porta. Sed in rhoncus lorem. Maecenas elementum vestibulum orci. Suspendisse potenti.

Nullam id finibus justo. Suspendisse porttitor imperdiet diam, nec tristique felis ornare vel. Aliquam non ante quis mi egestas posuere. Sed sed lectus erat. Nulla semper elit consequat eros commodo, a sagittis risus scelerisque. Aenean semper ipsum eget convallis malesuada. Sed cursus, quam et efficitur vulputate, orci leo faucibus nisl, et eleifend purus nibh id lorem.

Nullam finibus lectus hendrerit, auctor orci quis, scelerisque elit. Pellentesque elit arcu, lacinia eu nunc non, consequat auctor leo. Curabitur ac mi luctus, aliquam lacus nec, eleifend felis. Praesent sed varius odio. Aenean non tristique purus. Quisque ullamcorper felis sit amet nisl dictum volutpat. Cras sodales ante eu sapien rhoncus ultricies. Quisque nisl nisi, tempor sed mattis eu, tristique sit amet quam. Donec eu malesuada leo, eget consequat elit.

Curabitur tempor risus id cursus ullamcorper. Integer aliquet, nisi et consequat tincidunt, mi erat pulvinar lorem, quis porttitor felis quam quis turpis. Nam dapibus quam eu fermentum efficitur. Nam tristique elit vel dolor eleifend, eu eleifend lorem lacinia. Mauris malesuada commodo magna sed congue. Sed pellentesque nunc eu elementum rhoncus. Curabitur nunc nisi, ultricies vitae lobortis a, ultrices quis orci. Nunc tincidunt justo nec elementum fringilla. Nullam maximus nulla id elit rutrum, in vulputate nisl maximus. Sed tempus leo in ligula varius venenatis. Phasellus scelerisque vulputate quam quis convallis. Donec vestibulum malesuada lacus, ornare fermentum massa faucibus in. Duis hendrerit lacus ac lacus posuere pulvinar.

Suspendisse eu vulputate nibh. Suspendisse interdum suscipit nisi, ac ultrices quam gravida ac. Vivamus sed est quis purus feugiat auctor. Nam a libero in justo sodales rhoncus. Fusce tincidunt mi sit amet leo maximus accumsan. Donec sollicitudin rutrum urna non viverra. Phasellus fringilla ullamcorper tortor et feugiat. Aliquam pharetra non augue sit amet tempus. Donec eget enim malesuada, dapibus lectus non, accumsan est. Integer sit amet imperdiet nisl. Quisque interdum convallis auctor. Aliquam porta tempor porttitor. Mauris at turpis ac eros tincidunt maximus in eu elit. Donec tincidunt venenatis risus, eget eleifend quam dapibus facilisis. Morbi dignissim aliquam lacus. Quisque vel lobortis lorem, nec tempor enim.

Curabitur consectetur lacinia lectus, ut consequat dui tincidunt at. In hac habitasse platea dictumst. Maecenas eget consectetur sapien, sit amet sagittis ante. Morbi efficitur sem vel tortor auctor, et semper sem fringilla. Suspendisse molestie felis ac neque accumsan hendrerit. Quisque vel enim semper dolor iaculis tristique nec sed dolor. Vivamus a condimentum leo. Vestibulum venenatis sagittis risus, at condimentum libero aliquet eu. Nullam a pellentesque justo, eu facilisis lacus. Sed et purus augue. Mauris et erat ligula.

Aliquam consequat dui lorem, at molestie lorem venenatis aliquam. Nulla ut dui id felis efficitur fermentum. Donec vel ultrices eros. Pellentesque et dolor nec enim sollicitudin fringilla. Sed elementum magna massa, at fermentum urna sagittis quis. Sed laoreet facilisis nisi quis commodo. Etiam porta maximus libero non tristique.
    """.trimIndent()
    val test = PdfHandler()
    test.writeToNewPdf(text = text, fileName = "testPDF.pdf")

    // Add content to existing pdf
    test.addToPdf(lorem, "testPDF.pdf")
}

