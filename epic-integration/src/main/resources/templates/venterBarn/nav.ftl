<!DOCTYPE html>
<html lang="no">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>NAVs side</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-light shadow p-4">
        <h1>NAV</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../funksjonsvurdering/sidebar.ftl">
    </div>
    <div>
        <h3>Personer som er gravide:</h3>
        <br>
        <#if data??>
            <#list data?keys as key>
                Person ${key}: ${data[key]}
                <br>
                <br>
            </#list>
        <#else>
            Ingen gravide personer
            <br>
        </#if>
        <a href="/venter-barn">Gå tilbake til navigasjonsiden.</a>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>