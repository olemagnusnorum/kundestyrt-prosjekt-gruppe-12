<!DOCTYPE html>
<html lang="en">
<head>
    <title>NAV-innboks</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h3>Nye hendleser fra leger</h3>
    <p>INNBOKS</p>
    <br>
    <p>Questionaire</p>
    <#if Questionnaire??>
        <#list Questionnaire as Questionnaire>
            Item: ${Questionnaire}
            <br>
        </#list>
    </#if>
    <br>
    <p>Graviditet</p>
    <#if Pregnancy??>
        <#list Pregnancy as Pregnancy>
            Item: <a href="/nav-inbox/pregnancy/${Pregnancy}">${Pregnancy}</a>
            <br>
        </#list>
    </#if>
    <br>
    <a href="/doctor">Gå tilbake til legens side.</a>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>