<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Questionnaire</title>
    <style>
        .header {
            background-color: #9CB9E7;
        }
    </style>
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="header">
        <div class="col shadow p-4">
            <h1>Lege</h1>
        </div>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../shared/sidebar.ftl">
    </div>
    <div class="col">
        <h3>Spørsmål fra NAV</h3>

        <form action="/funksjonsvurdering/createQuestionnaireResponse/${questionnaire.id}" method="post">
            <#list questionnaire.item as question>
                <div class="form-group">
                    <b>${question.text}</b>
                    <input class="form-control" type="text" name="answer${question.linkId}" placeholder="Svar">
                    <br>
                </div>
            </#list>
            <br>
            <input type="submit" class="btn btn-primary" value="Send svarene til NAV">
        </form>
        <br>
        <br>
    </div>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>