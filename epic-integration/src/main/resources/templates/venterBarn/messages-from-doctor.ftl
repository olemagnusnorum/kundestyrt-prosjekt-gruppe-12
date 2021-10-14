<#ftl output_format="XML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Legeside - meldinger</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>NAVs meldinger</h1>
    <h3>Legen har send deg informasjon</h3>
    <p>${response}</p>
    <h3>Send et svar:</h3>
    <input type="text">
    <form action="/venter-barn/request-health-information-confirmation" method="post">
        <input type="submit" value="Send melding til legen">
    </form>
    <br>
    <br>
    <a href="/venter-barn/nav">Gå tilbake til NAVs side</a>
    <br>
    <a href="/venter-barn">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>