package com.backend

import kotlin.test.*
import com.backend.plugins.*

class parseBundleToResourceTest {
    private val epicCommunication = EpicCommunication()
    @Test
    fun `parseBundleToResource should run`() {
        epicCommunication.parseBundleToResource("""{"resourceType": "Bundle","id": "bundle-example","meta": {"lastUpdated": "2014-08-18T01:43:30Z"},"type": "searchset","total": 1,"link": [{"relation": "self","url": "https://example.com/base/MedicationRequest?patient=347&_include=MedicationRequest.medication&_count=2"},{"relation": "next","url": "https://example.com/base/MedicationRequest?patient=347&searchId=ff15fd40-ff71-4b48-b366-09c706bed9d0&page=2"}],"entry": [{"fullUrl": "https://example.com/base/Communication/fm-solicited","resource": {"resourceType": "Communication","id": "eQtjP5dExSGL8QY3jIixZo0TrO52tQfNEGkoWTOJdWCU3","basedOn": [{"reference": "ServiceRequest/eZykr93PG.4eADHuIA7x31kTgnBtaXdav57aDWVlvDWvi-TiVRQGvTBsmjwpvM8n73"}],"partOf": [{"reference": "Task/ebvg8Qy8tsSAz7oLPJgZXUN3gKXtUQEDEo-3.OI.uuPcHc7JRfVOphJCVs.wEo4DF3"}],"status": "in-progress","subject": {"reference": "Patient/e5CmvJNKQAN-kUr-XDKfXSQ3","display": "Patient, Bravo"},"encounter": {"reference": "Encounter/ePsDBvsehVaICEzX4yNBTGig.9WVSJYHW-td1KddCl1k3"},"sent": "2021-01-25T06:16:23Z","recipient": [{"reference": "Organization/eXn64I93.1fbFG3bFDGaXbA3","display": "Ven B Cbo Transport 5 (Fhir)"}],"sender": {"reference": "Practitioner/ectBdL9yLwfiRop1f5LsU6A3","display": "Susanna Sammer, MSW"},"payload": [{"contentString": "Can you send us more information?\r\n"}]}}]}""")
    }
}