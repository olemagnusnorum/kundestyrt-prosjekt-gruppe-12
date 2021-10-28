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
        <h3>Tilgjengelige skjema</h3>
        <br>
            <ul class="list-group">
            <a class="text-white btn-floating btn-fb" href="doctor-form-pregnant">
                <li class="list-group-item d-flex">
                    <span class="p-2">
                        Registrer graviditet
                    </span>
                    <span class="ml-auto p-2">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-caret-right" viewBox="0 0 16 16">
                            <path d="M6 12.796V3.204L11.481 8 6 12.796zm.659.753 5.48-4.796a1 1 0 0 0 0-1.506L6.66 2.451C6.011 1.885 5 2.345 5 3.204v9.592a1 1 0 0 0 1.659.753z"/>
                        </svg>
                    </span>
                </li>
            </a>
            <a class="text-white btn-floating btn-fb" href="doctor-form-pregnant-update">
                <li class="list-group-item d-flex">
                    <span class="p-2">
                        Oppdater graviditet
                    </span>
                    <span class="ml-auto p-2">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-caret-right" viewBox="0 0 16 16">
                            <path d="M6 12.796V3.204L11.481 8 6 12.796zm.659.753 5.48-4.796a1 1 0 0 0 0-1.506L6.66 2.451C6.011 1.885 5 2.345 5 3.204v9.592a1 1 0 0 0 1.659.753z"/>
                        </svg>
                    </span>
                </li>
            </a>
        </ul>
    </div>
    <br>
    <br>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>