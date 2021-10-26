<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Legeside</title>
    <base href="/venter-barn/">
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Legeside - Graviditetsskjema</h1>

    <form action="/venter-barn/doctor-form-pregnant" method="post">
        <p>Personnummer:</p>
        <input name="id" type="text" placeholder="Personnummer"><br><br>
        <p>Medisinsk beskrivelse:</p>
        <input name="note" type="text" placeholder="Beskrivelse"><br><br>
        <p>Onset Date:</p>
        <input name="onsetDate" type="text" placeholder="YYYY-mm-dd"><br><br>
        <p>Abatement Date:</p>
        <input name="abatementDate" type="text" placeholder="YYYY-mm-dd"><br><br>
        <input type="submit" value="Registrer graviditet">
        <#if error??>
            <p>${error}</p>
        </#if>
    </form>
    <br>
    <br>
    <a href="/venter-barn">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>