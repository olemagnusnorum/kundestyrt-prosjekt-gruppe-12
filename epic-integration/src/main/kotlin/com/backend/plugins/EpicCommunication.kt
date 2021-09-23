package com.backend.plugins

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient

fun requestEpic() : String {
    return "1"
}

//Maybe TODO: Find more general parsing. Ex.: From Bundle to whatever object is in it.
fun parseBundleToPatient(xmlMessage: String): Patient {

    val ctx = FhirContext.forR4()

    // The hapi context object is used to create a new XML parser
    // instance. The parser can then be used to parse (or unmarshall) the
    // string message into a Patient object
    val parser: IParser = ctx.newXmlParser()
    parser.setPrettyPrint(true)
    val jsonParser: IParser = ctx.newJsonParser()
    jsonParser.setPrettyPrint(true)

    val bundle: Bundle = parser.parseResource(Bundle::class.java, xmlMessage)

    val patient: Patient = bundle.entry[0].resource as Patient

    println(patient.name[0].family)

    return patient
}

