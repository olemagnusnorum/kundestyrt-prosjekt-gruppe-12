<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Questionnaire</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Questionnaire fra NAV</h1>

    <form action="/funksjonsvurdering/createQuestionnaireResponse/${questionnaire.id}" method="post">
        <#list questionnaire.item as question>
            <p> ${question.text} </p>
            <input type="text" name="answer${question.linkId}">
            <br>
        </#list>
        <br>
        <input type="submit" value="Send svarene!">
    </form>

    <br>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>