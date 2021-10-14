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

        <h3>Innboks</h3>
        <#list questionnaires as questionnaire>
            <a href="/functional-analysis/doctor-inbox/${questionnaire.id}"> ${questionnaire.title} </a>
        </#list>

    <#else>

        <h3>Velg pasient</h3>
        <form action="/functional-analysis/doctor-inbox" method="post">
            <input name="id" type="text">
            <input type="submit">
        </form>

    </#if>

    <br>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>