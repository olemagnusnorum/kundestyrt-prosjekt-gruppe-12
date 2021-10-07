<!DOCTYPE html>
<html lang="en">
<head>
    <title>Helseplattformen</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Helseplattformen | Lege</h1>
    <h3>Se melding fra NAV</h3>
    <form action="/messages-from-nav" method="get">
        <input type="submit" value="Se melding">
    </form>
    <br>

    <h3>Dine pasienter</h3>
    <#if patient??>
        <p>${patient.name[0].text}
    </#if>
    <br>
    <h3>Registrer testpasient</h3>
    <form action="/create-patient" method="post">
        <input name="given" type="text" placeholder="Given name"><br><br>
        <input name="family" type="text" placeholder="Family name"><br><br>
        <input name="identifierValue" type="text" placeholder="xxx-xx-xxxx"><br><br>
        <input type="submit" value="Registrer pasient">
    </form>
    <br>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>