<!DOCTYPE html>
<html lang="en">
<head>
    <title>NAVs side</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>NAV</h1>
    <h3>Personer som er gravide</h3>
    <br>
    <#if data??>
        <#list data?keys as key>
            person ${key}: ${data[key]}
            <br>
            <br>
        </#list>
    <#else>
        Ingen gravide personer
        <br>
    </#if>
    <a href="/venter-barn">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>