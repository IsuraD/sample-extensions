# **User Management Service**

**Start the service**


`cd user-mgt-service`

`mvn spring-boot:run`


**Samples Curl commands**

_**Add user**_

`curl --location --request POST 'http://localhost:8081/users/' \
--header 'authorization: Bearer cd7ad21e-08ac-3d2a-b3cf-104be8e18620' \
--header 'accept: application/json' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "Isura",
    "lastName": "Dilhara",
    "email": "isura@mail.com",
    "dateOfBirth": "1990-03-03"
}'`


**_Get All users_**

`curl --location --request GET 'http://localhost:8081/users/' \
--header 'authorization: Bearer cd7ad21e-08ac-3d2a-b3cf-104be8e18620' \
--header 'accept: application/json' \
--header 'Content-Type: application/json'`


**_Get a user_**

`curl --location --request GET 'http://localhost:8081/users/4' \
--header 'authorization: Bearer cd7ad21e-08ac-3d2a-b3cf-104be8e18620' \
--header 'accept: application/json' \
--header 'Content-Type: application/json'`

**_Update user_**

`curl --location --request PUT 'http://localhost:8081/users/4' \
--header 'authorization: Bearer cd7ad21e-08ac-3d2a-b3cf-104be8e18620' \
--header 'accept: application/json' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 4,
    "firstName": "IsuraDilahra",
    "lastName": "Dilhara",
    "email": "isura@mail.com",
    "dateOfBirth": "1990-03-12"
}'`


**_Delete User_**

`curl --location --request DELETE 'http://localhost:8081/users/4' \
--header 'authorization: Bearer cd7ad21e-08ac-3d2a-b3cf-104be8e18620' \
--header 'accept: application/json' \
--header 'Content-Type: application/json'`