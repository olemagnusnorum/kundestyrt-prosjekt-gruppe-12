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

    <#if condition??>
        <form action="/venter-barn/doctor-form-pregnant-update" method="post">
            <p>Personnummer: ${id}</p>
            <p>Medisinsk beskrivelse:</p>
            <input name="note" type="text" placeholder="Beskrivelse" value="${note!""}"><br><br>
            <p>Abatement Date:</p>
            <input name="abatementDate" type="text" placeholder="YYYY-mm-dd" value="${abatement!""}"><br><br>
            <input type="submit" value="Oppdater graviditet">
        </form>
    <#else>
        <form action="/venter-barn/doctor-form-pregnant-update" method="get">
            <p>Personnummer:</p>
            <input name="id" type="text" placeholder="Personnummer"><br><br>
            <input type="submit" value="Finn pasient">
        </form>
    </#if>
    <br>
    <br>
    <a href="/venter-barn">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>