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
        <#include "../shared/sidebar.ftl">
    </div>
    <div class="col">
        <h3>Skjema for oppdatering av graviditet</h3>

        <#if condition??>
            <form action="/venter-barn/doctor-form-pregnant-update" method="post">
                <div class="form-group">
                    <b>Personnummer</b>
                    <input class="form-control" name="id" type="text" placeholder="Personnummer" value="${id}" readonly>
                </div>
                <br>
                <div class="form-group">
                    <b>Medisinsk beskrivelse</b>
                    <input class="form-control" name="note" type="text" placeholder="Beskrivelse" value="${note!""}">
                </div>
                <br>
                <div class="form-group">
                    <b>Termindato</b>
                    <input class="form-control" name="abatementDate" type="text" placeholder="YYYY-mm-dd" value="${abatement!""}">
                </div>
                <br>
                <input class="btn btn-primary" type="submit" value="Oppdater graviditet">
            </form>
        <#else>
            <br>
            <form action="/venter-barn/doctor-form-pregnant-update" method="get">
                <b>Personnummer</b>
                <input class="form-control" name="id" type="text" placeholder="Personnummer">
                <br>
                <input class="btn btn-primary" type="submit" value="Finn pasient">
            </form>
            <#if error??>
                <p>${error}</p>
            </#if>
        </#if>
        <br>
        <br>
    </div>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>