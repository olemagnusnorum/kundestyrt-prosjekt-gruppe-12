<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Helseplattformen</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Lege</h1>

    <#if patientId??>

        <h3>${patient.name[0].family}</h3>
        <br>

        <h3>Innboks</h3>
        <#if questionnaireResponses??>
            <#list questionnaires as questionnaire>
                <a href="/funksjonsvurdering/doctor-inbox/${questionnaire.id}"> ${questionnaire.title} </a>
            </#list>
        <#else>
            <p>Ingen nye meldinger.
        </#if>

    <#else>

        <h3>Velg pasient</h3>
        <form action="/funksjonsvurdering/doctor-inbox" method="post">
            <input name="patientId" type="text">
            <input type="submit">
        </form>

    </#if>

    <br>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>