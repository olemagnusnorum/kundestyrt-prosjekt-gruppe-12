# kundestyrt-prosjekt-guppe-12
Customer driven project group 12

Group members:  
Forbord, Ole Marius (omforbor@stud.ntnu.no)  
Kristoffersen, Markus Hvidsten (markushk@stud.ntnu.no)  
Langli, Karoline Lillevestre (karolill@stud.ntnu.no)  
Nguyen, Kenny (kennyn@stud.ntnu.no)  
Norum, Ole-Magnus Vian (omnorum@stud.ntnu.no)  
Storruste, Amund Kulsrud (amundks@stud.ntnu.no)  
Vian, Matias Johansen (matiasjv@stud.ntnu.no)  
Vågen, Magnus Morud (magnusmv@stud.ntnu.no)  

## Dependencies

1.	If you do not have an JDK installation, you will have to download one.
2.	Download JetBrain Intellij.
3.	Download Docker.

## Installing the server

The application and the HAPI server need to be able to communicate both ways, and for this to work we run them both locally. 

1. Download the image of the HAPI server by running 

```bash
docker pull hapiproject/hapi:latest
```
    
2. To start a new container running the server, run the command 
        
```bash
docker run -p 8000:8080 hapiproject/hapi:latest.
```


To enable the subscription resource for the local HAPI server we have to edit the application.yaml file. 
     
3. First we got to open the docker terminal by running the command 
    
```bash
docker exec -it "name-of-docker-container" bash 
```   

in another terminal window. “name-of-docker-container” is found in the Docker application. It will be a random adjective paired with a random noun. 
    
4. Since this docker container does not come with a terminal based text editor, we have to download one before we can edit the application.yaml file. Run the commands 
        
```bash
apt-get update and apt-get install vim 
```
        
5. Now navigate and open the application.yaml file with vim by running 
        
```bash
vim webapps/ROOT/WEB-INF/classes/application.yaml 
```

6. Uncomment the lines "subscription:" and "resthook\_enabled: true" by typing i and removing the hashtags on those two lines. 

7. To exit vim press escape and enter :wq. 

8. Restart the docker container

The HAPI server should now be able to use the subscription resource.

## Installing the application

1.  Clone our repository usingg 

```bash
vim webapps/ROOT/WEB-INF/classes/application.yaml 
```
2. Run the Application.kt file within Intellij with the server running in a docker container. The file is located in epic-integration/src/main/kotlin/com/backend


## Usage

The primary page opens with both case scenarios. These cases are also found on the sidebar on the left-hand side, and each party is represented within these cases. Because our application primarily serves to test the FHIR API, it is not equipped with luxuries like form validation and verbose responses. It is therefore vital that each input is logical and correct.

<img width="1366" alt="Parental%20benefits%20-%20The%20doctor's%20page" src="https://user-images.githubusercontent.com/56272714/141292408-8fcea909-c67f-48e3-a966-b091b2bea348.png">

Note that most parties need a valid social security number to “log in” or to choose a patient/person. Valid SSNs are 07069012345 (Kari Nordmann) and 07069012346 (Ola Nordmann).

<img width="508" alt="Inputting%20the%20social%20security%20number" src="https://user-images.githubusercontent.com/56272714/141292580-e6456552-c980-4a68-94b7-0fbe2c468569.png">

Both cases are straightforward. Within the parental benefits case a patient can only apply for benefits after a pregnancy is registered by the doctor, and a pregnancy condition can only be updated after it is created. Note that a valid YYYY-MM-DD date has to be inputted.

Similarly within the functionality analysis the communication chain will start by NAV requesting information from the doctor, and the questionnaires will only be visible after NAV has sent them.
