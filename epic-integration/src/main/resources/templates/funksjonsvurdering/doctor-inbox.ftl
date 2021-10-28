<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Helseplattformen</title>
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-secondary p-5">
        <h1 class="text-light">Kundestyrt prosjekt | Demo</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "*/sidebar.ftl"></div>
    <div class="col">
        <h1>Helseplattformen | Lege</h1>

        <#if patient??>

            <h5>Pasient: ${patient.name[0].given[0]} ${patient.name[0].family}</h5>
            <br>
            <h3>Innboks</h3>
            <#if questionnaires??>
                <#list questionnaires as questionnaire>
                    <a href="/funksjonsvurdering/doctor-inbox/${questionnaire.id}"> ${questionnaire.title} </a>
                    <br>
                </#list>
            <#else>
                <p>Ingen nye meldinger.</p>
            </#if>

        <#else>
            <br>
            <h3>Velg pasient</h3>
            <br>
            <div class="row">
                <div class="col">
                    <form action="/funksjonsvurdering/doctor-inbox" method="post">
                        <input name="patientId" placeholder="Fødselsnummer" type="text">
                        <input class="btn rounded border py-1 px-4" type="submit">
                    </form>
                </div>
            </div>

        </#if>
        <br>
        <br>
        <a href="/funksjonsvurdering">Gå tilbake til navigasjonsiden.</a>
    </div>
    <div class="col"></div>

</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>