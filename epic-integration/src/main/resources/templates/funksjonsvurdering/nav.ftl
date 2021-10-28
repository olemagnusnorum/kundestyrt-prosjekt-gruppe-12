<!DOCTYPE html>
<html lang="en">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <title>NAVs side</title>
</head>
<body style="font-family: sans-serif">
<div class="row mb-5">
    <div class="col bg-light shadow p-4">
        <h1>NAV</h1>
    </div>
</div>
<div class="row">
    <div class="col">
        <#include "../shared/sidebar.ftl"></div>
    <div class="col">

        <#if patient??>

            <h3>Pasient: ${patient.name[0].given[0]} ${patient.name[0].family}</h3>
            <br>
            <h3>Innboks</h3>
            <#if questionnaireResponses??>
                <ul class="list-group">
                <#list questionnaireResponses as questionnaireResponse>
                        <a class="text-white btn-floating btn-fb" href="/funksjonsvurdering/nav/${questionnaireResponse.id}">
                            <li class="list-group-item d-flex">
                                <span class="p-2">
                                    ${questionnaireTitles[questionnaireResponse?index]}
                                </span>
                                <span class="ml-auto p-2">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-caret-right" viewBox="0 0 16 16">
                                        <path d="M6 12.796V3.204L11.481 8 6 12.796zm.659.753 5.48-4.796a1 1 0 0 0 0-1.506L6.66 2.451C6.011 1.885 5 2.345 5 3.204v9.592a1 1 0 0 0 1.659.753z"/>
                                    </svg>
                                </span>
                            </li>
                        </a>
                </#list>
                </ul>
            <#else>
                <p>Ingen nye meldinger.
            </#if>

            <br>
            <h3>Send forhåndslagde spørsmål</h3>
            <#list predefinedQuestionnaires as questionnaire>
                <div class="card">
                    <div class="card-body">
                        <form action="/funksjonsvurdering/create-predefined-questionnaire" method="post">
                            <h5 class="card-title"> ${questionnaire.title} </h5>
                            <#list questionnaire.item as question>
                                <p class="card-text mb-1"> ${question.text} </p>
                            </#list>
                            <input hidden name="patientId" type="text" value="${patient.id}">
                            <input hidden name="questionnaireId" type="text" value="${(questionnaire.id?split("/"))[5]}">
                            <input class="btn btn-primary mt-3" type="submit" value="Send disse spørsmålene">
                        </form>
                    </div>
                </div>
                <br>
            </#list>

        <#else>

            <h3>Velg bruker</h3>
            <br>
            <div class="row">
                <div class="col">
                    <form action="/funksjonsvurdering/nav" method="post">
                        <input class="form-control" name="patientId" placeholder="Fødselsnummer" type="text">
                        <br>
                        <input class="btn btn-primary" type="submit" value="Se brukerens side">
                    </form>
                </div>
            </div>

        </#if>
        <br>
        <br>
    </div>
    <div class="col"></div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous"></script>
</body>
</html>