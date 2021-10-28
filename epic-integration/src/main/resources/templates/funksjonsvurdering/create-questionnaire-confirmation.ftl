<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Opprettet questionnaire</title>
</head>

<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-secondary p-5">
        <h1>Kundestyrt prosjekt | Demo</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../shared/sidebar.ftl">
    </div>
    <div class="col">
        <h3>Du har opprettet questionnaire ${response}</h3>
        <br>
        <a href="/funksjonsvurdering/nav">Gå tilbake til NAV sin side.</a>
        <br>
        <a href="/funksjonsvurdering">Gå tilbake til navigasjonsiden.</a>
    </div>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>