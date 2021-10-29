<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Legeside</title>
    <base href="/venter-barn/">
    <style>
        .header {
            background-color: #D4D7D8;
        }
    </style>
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="header">
        <div class="col shadow p-4">
            <h1>Bruker</h1>
        </div>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../shared/sidebar.ftl">
    </div>
    <div class="col">
        <h3>Hei, ${name}!</h3>
        <#if condition??>
            <p>${condition}</p>
            <p><b>Termindato:</b> ${due_date}</p>

            <form action="/venter-barn" method="get">
                <input class="btn btn-primary" type="submit" value="Søk foreldrepenger">
            </form>
        <#else>
            <p>Du kan ikke søke om foreldrepenger.</p>
        </#if>
        <br>
        <br>
    </div>
    <div clasS="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>