<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Min side</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<div>
    <h1>Nav | Søk om foreldrepenger</h1>
    <h2>Hei ${name}!</h2>
    <#if condition??>
        <p><b>Condition:</b> ${condition.code.text}</p>
        <p><b>Termindato:</b> ${due_date}</p>

        <form action="/" method="get">
            <input type="submit" value="Søk foreldrepenger">
        </form>
    <#else>
        <p>Du kan ikke søke om foreldrepenger.</p>
        <form action="/" method="get">
            <input type="submit" value="Søk foreldrepenger">
        </form>
    </#if>
    <br>
    <br>
    <a href="/">Gå tilbake til navigasjonsiden.</a>
</div>
</body>
</html>