<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Questionnaire</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Questionaire-response from Doctor</h1>

    <#list questions as question>
        <p> Question ${question?index+1} : ${question} </p>
        <p> Answer: ${answers[question?index]} </p>
        <br>
    </#list>

    <br>
    <br>
    <a href="/funksjonsvurdering/nav">Gå tilbake til NAVs side</a>
</div>
</body>
</html>