# SD2425 project

This repository includes the API and a set of files that should be used in project 1.

* ```test-sd-tp1.bat``` / ```test-sd-tp1.sh``` :  script files for running the project in Windows and Linux/Mac
* ```fctreddit.props``` : file with information for starting servers
* ```Dockerfile``` : Dockerfile for creating the docker image of the project
* ```hibernate.cfg.xml``` : auxiliary file for using Hibernate
* ```pom.xml``` : maven file for creating the project

------------------------------------------------------

mvn clean compile assembly:single docker:build

docker run -h serv --network sdnet -p 8080:8080 sd2425-tp1-api-65428-65466
docker run -it --network sdnet sd2425-tp1-api-65428-65466 /bin/bash

------------------------------------------------------
TESTES USERS

java -cp sd2425.jar fctreddit.impl.clients.CreateUserClient ruisilva Rui rs@mail.com 1234
java -cp sd2425.jar fctreddit.impl.clients.CreateUserClient marialeal Maria ml@mail.com 4321
java -cp sd2425.jar fctreddit.impl.clients.UpdateUserClient marialeal 4321 Mary mleal@mail.com 54321
java -cp sd2425.jar fctreddit.impl.clients.GetUserClient marialeal 54321
java -cp sd2425.jar fctreddit.impl.clients.DeleteUserClient marialeal 54321
java -cp sd2425.jar fctreddit.impl.clients.SearchUsersClient marial

------------------------------------------------------
TESTES CONTENT

