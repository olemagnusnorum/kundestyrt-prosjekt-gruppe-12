<!DOCTYPE html>
<html lang="en">
<head>
    <title>NAVs side</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>NAV</h1>
    <p>Derrick Lin har søkt om sykepenger.</p>
    <h3>Be om helseinfo</h3>
    <input type="text">
    <form action="/request-health-information-confirmation" method="post">
        <input type="submit">
    </form>
    <h3>Se melding fra lege</h3>
    <form action="/messages-from-doctor" method="post">
        <input type="submit">
    </form>
    <br>
    <br>
    <a href="/funksjonsvurdering">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>