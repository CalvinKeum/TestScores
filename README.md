# Test Scores

## Overview
This project provides APIs for managing students and exams. It includes functionalities to retrieve student information, exam details, and perform various operations related to student and exam data.

## Features
- Retrieve a list of all students.
- Calculate and retrieve average scores for students across all exams.
- Retrieve a list of all exams.
- Calculate and retrieve average scores of all studens for an exam.
- Rate limiting functionality to prevent excessive API requests.

## Technologies Used
- Java
- Spring Boot
- Maven
- Resilience4j (for rate limiting)
- Swagger (for API documentation)

## Setup and Usage
To run this project locally, follow these steps:

1. Clone the repository:
   ```
   git clone <repository-url>

2. Navigate to the project directory and run :
    ```
    mvn spring-boot:run

This will compile the project and start the Spring Boot application. Once the application is up and running, you can access the APIs locally.
Make sure you have Maven installed and configured properly on your system before running this command.

3. To run tests (optional), execute the following command:

   ```
   mvn clean test

## API
You can access the API documentation at this URL:

    GET http://localhost:8080/test-scores-documentation
### API Endpoints
- Retrieve all students
    ``` 
    GET http://localhost:8080/api/v1/students?skip=0&limit=20&sort_order=asc
  
- Get the average score for a specific student
    ```
    GET http://localhost:8080/api/v1/students/{studentId}
- Retrieve all exams
    ```
    GET http://localhost:8080/api/v1/exams?skip=0&limit=20&sort_order=asc
- Retrieve details of a specific exam
    ```
    GET http://localhost:8080/api/v1/exams/{exam}

### Rate Limiting
The APIs are protected by rate limiting to prevent abuse. If the rate limit is exceeded, a 429 Too Many Requests response will be returned.