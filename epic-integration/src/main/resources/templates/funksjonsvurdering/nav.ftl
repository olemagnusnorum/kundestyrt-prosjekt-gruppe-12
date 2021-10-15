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
        <a href="/funksjonsvurdering/create-questionnaire">Lag et questionnaire som skal sendes til Legen.</a>
        <h3>Innboks</h3>
        <#if questionnaireResponses??>
            <#list questionnaireResponses as questionnaireResponse>
                <a href="/funksjonsvurdering/nav/${questionnaireResponse.id}"> ${questionnaires[questionnaireResponse?index].title} </a>
            </#list>
        <#else>
            <p>Ingen nye meldinger.
        </#if>

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