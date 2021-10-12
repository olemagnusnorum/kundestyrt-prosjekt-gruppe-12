<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Questionnaire</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Questionaire for NAV</h1>
    <!--
    <h3>Se melding fra NAV</h3>
    <form action="/messages-from-nav" method="get">
        <input type="submit" value="Se melding">
    </form>
    -->


    <#if true>
        <h3>Registrer Questionnaire</h3>
        <form action="/create-questionnaire" method="post">
            <input name="question1" type="text" placeholder="Question 1"><br><br>
            <input name="question2" type="text" placeholder="Question 2"><br><br>
            <input type="submit" value="Registrer questionnaire">
        </form>
    </#if>
    <br>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>