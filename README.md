# document-analyser

## Overview
This project and application is a RESTful Spring Boot application where the user can:
- Create a Dictionary of words
- Add or remove words from a Dictionary
- Get an individual Dictionary
- Get all Dictionaries
- Submit a Job to search through a text for words in a dictionary
- Get the Job to retrieve status and results

Note:
- An embedded DB is used and data is persisted between restarts of the application
- An in-memory DB is used for the Spring Integration tests.

## How to use application
To run the app there are 3 options:
- Run from intellij (or other IDE)
- Execute: mvn clean spring-boot:run
- Run the packaged jar found in the releases of this repo

**Please note - the application runs on port 7007**

A postman collection is available with sample requests: https://github.com/stephengibson83/document-analyser/blob/main/src/test/postman/Ohalo%20Dictionary.postman_collection.json

Requests to execute:
- Create a dictionary first. e.g. POST to http://localhost:7007/v1/dictionary - this will return the Dictionary and its ID.
- Submit a search job. e.g. POST to http://localhost:7007/v1/job/search/submit - this will return the Job and its ID
- Lookup the job (GET to http://localhost:7007/v1/job/{{job1_id}}) to check on status and get results


## Instructions for building the application
The application is a Spring Boot app. The following is required to be installed on the build machine:
- Java 17
- Maven verion 3.x

To build the application locally, execute the command:
- mvn clean package

## Assumptions
- When finding matches for dictionary words in the input text, if a word in the dictionary is part of a word in the text, that will be considered a match. e.g. if the dictionary word is "the" and a word in the text is "these", that will be considered a match.
- The result of a Job should be saved to a persistent DB.

## Future enhancements
- when a job is complete it could perform an HTTP Callback (Webhook) to the client notifying it that the job is complete
- the search algorithm itself is fairly basic, this could be optimised.
- integrate with a real DB.
- The queue for asynchronous tasks is not persistent and items in the queue would be lost if the application crashed.  The queue could be separate from the application and shared between multiple instances.
- Add the ability to retry a failed job.
- Add more detail about why a job failed in the response to the client.
- Restrict that a Dictionary cannot be deleted if it is being used by an in progress job.
- The input text for a job should not be supplied in the HTTP request. Instead the request would contain a reference to a file where the text could be streamed from.
- Add operation to rename a Dictionary
- Add created/updated timestamps to a Job
- For the "getAllDictionaries" operation, make this a paged response.
- Add MDC logging with JSON format that is easier to search when ingesting the logs to services such as CloudWatch or Kibana
- Add client Authentication & Authorisation

## Citations
- https://www.baeldung.com/java-in-memory-databases
- https://stackoverflow.com/questions/64148644/hsqldb-persistence-in-a-spring-project
- https://www.baeldung.com/java-jpa-persist-string-list
- https://www.ohalo.co/blog/securing-fintech-advanced-data-strategies-for-leaders - for sample input text