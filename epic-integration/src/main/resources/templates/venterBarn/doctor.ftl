<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Legeside</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Lege</h1>
    <!--
    <h3>Se melding fra NAV</h3>
    <form action="/messages-from-nav" method="get">
        <input type="submit" value="Se melding">
    </form>
    -->

    <#if patientId??>
        <h3>Pasient ${name}</h3>
        <#if pregnancy??>
            <p>Pasienten er gravid</p>
        <#else>
            <form action="/venter-barn/create-pregnancy" method="post">
                <input type="submit" value="Registrer graviditet">
            </form>
        </#if>
    <#else>
        <h3>Registrer testpasient</h3>
        <form action="/venter-barn/create-patient" method="post">
            <input name="given" type="text" placeholder="Given name"><br><br>
            <input name="family" type="text" placeholder="Family name"><br><br>
            <input name="identifierValue" type="text" placeholder="xxx-xx-xxxx"><br><br>
            <input type="submit" value="Registrer pasient">
        </form>
    </#if>
    <br>
    <br>
    <a href="/venter-barn">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>