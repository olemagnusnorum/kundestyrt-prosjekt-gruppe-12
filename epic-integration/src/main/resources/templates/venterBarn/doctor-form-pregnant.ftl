<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Legeside</title>
    <base href="/venter-barn/">
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-light shadow p-4">
        <h1>Lege</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../funksjonsvurdering/sidebar.ftl">
    </div>
    <div class="col">
        <h3>Skjema for å registrere graviditet</h3>

        <form action="/venter-barn/doctor-form-pregnant" method="post">
            <div class="form-group">
                <b>Personnummer</b>
                <input class="form-control" name="id" type="text" placeholder="Personnummer">
            </div>
            <br>
            <div class="form-group">
                <b>Medisinsk beskrivelse</b>
                <input class="form-control" name="note" type="text" placeholder="Beskrivelse">
            </div>
            <br>
            <div class="form-group">
                <b>Start dato for graviditet</b>
                <input class="form-control" name="onsetDate" type="text" placeholder="yyyy-mm-dd">
            </div>
            <br>
            <div class="form-group">
                <b>Termindato</b>
                <input class="form-control" name="abatementDate" type="text" placeholder="yyyy-mm-dd">
            </div>
            <br>
            <input class="btn btn-primary" type="submit" value="Registrer graviditet">
            <#if error??>
                <p>${error}</p>
            </#if>
        </form>
        <br>
        <br>
    </div>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>