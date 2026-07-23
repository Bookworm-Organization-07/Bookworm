Conventions to be followed by everyone 


#Folder Structure

Bookworm

  backend-java/
  frontend/
  database/
  docs/

==========================

#Branch Naming

feature/auth

feature/product

feature/contributor

feature/cart

feature/order

feature/shelf

feature/rental

feature/library

feature/review

feature/revenue

feature/admin

===========================

#Commit Messages

feature : Added login API

fix: Fixed cart calculation

refactor: Improved ProductService

docs: Updated README

=============================

#Java Naming

UserController

UserService

UserRepository

User


Class Names → PascalCase

Variables (Java Fields)→ camelCase

Constants → UPPER_CASE

SQL Column namings -> snake_case

================================

# Package Structure 

controller

service

repository

entity

dto

mapper

config

exception

security

util

=====================================

#API Convention:-

GET    /api/products

GET    /api/products/{id}

POST   /api/products

PUT    /api/products/{id}

DELETE /api/products/{id}

==========================================

#DTO Convention

UserRequest

UserResponse

ProductRequest

ProductResponse

============================================

#Entity Naming

User

Product

Category

=======================================


# Daily Workflow

develop se latest code pull.

Apni feature/... branch banao.

Kaam complete karo.

Push to feature branch.

PR → develop.

Review.

Merge.
Jab stable ho → PR develop → main.

