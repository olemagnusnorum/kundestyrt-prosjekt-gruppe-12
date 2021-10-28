<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>Pasientinnlogging - Venter Barn Demo</title>
    <base href="/venter-barn/">
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-light shadow p-4">
        <h1>Bruker</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../funksjonsvurdering/sidebar.ftl">
    </div>
    <div class="col">
        <h3>Innlogging</h3>
        <form action="patient" method="get">
            <input class="form-control" name="id" type="text" placeholder="Personnummer">
            <br>
            <input class="btn btn-primary" type="submit" value="Login">
        </form>
        <br>
        <br>
    </div>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>