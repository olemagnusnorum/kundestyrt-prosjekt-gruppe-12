<!DOCTYPE html>
<html lang="en">
<head>
    <title>Opprett pasient</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h3>Du har opprettet en pasient</h3>
    <p>Familienavn: ${response}</p>
    <br>
    <form action="/create-condition" method="post">
            <input name="note" type="text" placeholder="Kommentar">
            <input name="id" type="text" value=${id} readonly><br><br>
            <input type="submit" value="Pasient er gravid">
        </form>
    <br>
    <a href="/doctor">Gå tilbake til legens side.</a>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>