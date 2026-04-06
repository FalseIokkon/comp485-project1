# Yunzog Dashboard

## Project Structure

```
dashboard/
├── pom.xml
├── yunzog.db

├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── App.java
│   │   │   ├── MainController.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── OverviewController.java
│   │   │   │   ├── FinanceController.java
│   │   │   │   └── ... other controllers
│   │   │   │
│   │   │   ├── dao/
│   │   │   │   ├── OverviewDao.java
│   │   │   │   ├── FinanceDao.java
│   │   │   │   └── ... other DAOs
│   │   │   │
│   │   │   ├── db/
│   │   │   │   ├── DB.java
│   │   │   │   ├── DatabaseInitializer.java
│   │   │   │   ├── DatabaseSeeder.java
│   │   │   │   └── seed/
│   │   │   │       ├── OrganizationSeed.java
│   │   │   │       └── ... other seed classes
│   │   │   │
│   │   │   └── model/
│   │   │       └── DivisionKPI.java
│   │   │
│   │   └── resources/
│   │       ├── main.fxml
│   │       ├── overview-tab.fxml
│   │       └── ... other FXML files
```

## Requirements

* JDK 21 or newer
* Maven

## Run the Application

From dashboard/:

```
mvn clean javafx:run
```
