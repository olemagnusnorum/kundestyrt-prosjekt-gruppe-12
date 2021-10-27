<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Questionnaire</title>
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-light shadow p-4">
        <h1>Svar fra legen</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../shared/sidebar.ftl"></div>
    <div class="col">

        <#list questions as question>
            <b>${question} </b>
            <div class="card">
                <div class="card-body">${answers[question?index]}</div>
            </div>
            <br>
        </#list>

        <br>

        <div class="row">
            <a class="btn btn-primary btn-block" href="/funksjonsvurdering/nav">Gå tilbake til NAVs side</a>
        </div>
        <br>
        <div class="row">
            <a class="btn btn-outline-primary btn-block" href="/funksjonsvurdering/create-questionnaire/Patient/${patientId}/_history/1">Lag et nytt questionnaire som skal sendes til Legen</a>
        </div>
        <br>
    </div>
    <div class="col"></div>

</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>