<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Questionnaire</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
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
</body>
</html>