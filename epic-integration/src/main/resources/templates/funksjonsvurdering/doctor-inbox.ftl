<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Helseplattformen</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Lege</h1>

    <#if patient??>

        <h3>Pasient: ${patient.name[0].given[0]} ${patient.name[0].family}</h3>

        <h3>Innboks</h3>
        <#if questionnaires??>
            <#list questionnaires as questionnaire>
                <a href="/funksjonsvurdering/doctor-inbox/${questionnaire.id}"> ${questionnaire.title} </a>
                <br>
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
    <br>
    <a href="/funksjonsvurdering">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>