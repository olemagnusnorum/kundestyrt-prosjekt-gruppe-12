<!DOCTYPE html>
<html lang="en">
<head>
    <title>NAVs side</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>NAV</h1>
    <br>

    <#if patient??>

        <h3>Pasient: ${patient.name[0].given[0]} ${patient.name[0].family}</h3>
        <#--<a href="/funksjonsvurdering/create-questionnaire/${patient.id}">Lag et questionnaire som skal sendes til Legen.</a>-->

        <h3>Innboks</h3>
        <#if questionnaireResponses??>
            <#list questionnaireResponses as questionnaireResponse>
                <a href="/funksjonsvurdering/nav/${questionnaireResponse.id}"> ${questionnaireTitles[questionnaireResponse?index]} </a>
                <br>
            </#list>
        <#else>
            <p>Ingen nye meldinger.
            <br>
        </#if>

        <br>

        <h3>Send forhåndslagde spørsmål</h3>
        <#list predefinedQuestionnaires as questionnaire>
            <form action="/funksjonsvurdering/create-predefined-questionnaire" method="post">
                <p> ${questionnaire.title} </p>
                <#list questionnaire.item as question>
                    <p> ${question.text} </p>
                </#list>
                <input hidden name="patientId" type="text" value="${patient.id}">
                <input hidden name="questionnaireId" type="text" value="${(questionnaire.id?split("/"))[5]}">
                <input type="submit" value="Send disse spørsmålene">
            </form>
            <br>
        </#list>

    <#else>

       <h3>Velg pasient</h3>
       <form action="/funksjonsvurdering/nav" method="post">
           <input name="patientId" type="text">
           <input type="submit">
       </form>

    </#if>
    <br>
    <br>
    <br>
    <a href="/funksjonsvurdering">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>