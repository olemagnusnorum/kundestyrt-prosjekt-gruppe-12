<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Questionnaire</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-secondary p-5">
        <h1>Kundestyrt prosjekt | Demo</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "*/sidebar.ftl">
    </div>
    <div class="col">
        <h1>Questionaire for NAV</h1>

        <#if true>
            <h3>Registrer Questionnaire</h3>
            <form action="/funksjonsvurdering/create-questionnaire" method="post">
                <input name="question1" type="text" placeholder="Question 1"><br><br>
                <input name="question2" type="text" placeholder="Question 2"><br><br>
                <input name="question3" type="text" placeholder="Question 3"><br><br>
                <input type="submit" value="Registrer questionnaire">
            </form>
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