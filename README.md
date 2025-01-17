nstall docker: brew install --cask docker

Install mysql: docker pull mysql

Create DB: docker run --name mysql-container -e MYSQL_ROOT_PASSWORD=myrootpassword -e MYSQL_DATABASE=test -e MYSQL_USER=test -e MYSQL_PASSWORD=test -p 3306:3306 -d mysql
(if mysql-container is already running in the local docker then you get error that it's already running)

Run DB scripts-------->>>>>>>>>
- click mysql-container in the docker running in your local
- Go to exec tab
- mysql -u root -p
- myrootpassword (paste the root password)
- use databaseName;
- run the sql scripts here


<h1> Airline Check-In system - Using of DB transaction locks (mysql) </h1>

<table>
    <tr>
        <td>
        <h2> Description </h2>
        <p> This project is a simple implementation of an airline check-in system. It uses a MySQL database to store the information of the passengers and the seats. The system is designed to be used by the airline staff to check-in passengers. The system uses database transaction locks to ensure that only one staff member can check-in a passenger at a time. This prevents the system from double booking a seat. </p>
        </td>
    </tr>
</table>

<h2> Technologies Used </h2>
<ul>
    <li> Java 19 </li>
    <li> MySQL </li>
</ul>

<h2> How to Run </h2>
<ol>
    <li> Clone the repository </li>
    <li> Create a MySQL database and run the SQL script in the `database` folder to create the necessary tables </li>
    <li> Update the `src/main/resources/application.properties` file with your database connection details </li>
    <li> mvn clean install and Run the `Main` class in the `src/main/java/com/airline/checkin` package 
using java -jar ./target/checkin-1.0-SNAPSHOT-jar-with-dependencies.jar > log.txt
 </li>
</ol>

<h2> Application Flow </h2>
<p> The application flow is as follows: </p>
<ol>
    <li> Concurrent Checkin processor spins 120 threads for each user to book unassigned seat </li>
    <li> Each thread will try to book a seat for a user, calling SeatRepository bookSeat method</li>
    <li> SeatRepository bookSeat method will try to book a seat for a user, 
using the DB connection pool and uses a database transaction lock to ensure that only one thread can book a seat at a time </li>
    <li> If the seat is already booked, the thread will try to book another seat for the user </li>
    <li> Unassigned seat SELECT * FROM seats WHERE user_id is null order by id LIMIT 1 FOR UPDATE SKIP LOCKED query is used for db lock for update</li>
    <li> "for update" will lock the first unassigned seat for a single thread - db connection and update the seat for user using the DB transaction. This verion of the unassigned query will lock the unassigned seat row causing other threads to wait for DB lock to be released.</li>
    <li> "for update skip locked" will skip the lock for other threads to pick to next unassigned seat improving the performance</li>
    <li> "with db row lock", the thread contend and does duplicate seat assignment.</li>
    <li> If DB connection is limited, the thread wait for getting released connection. once connection is available, db transaction is initiated </li>
    <li> The prototype demonstrate applications threads in conjunction with DB connection pool to perform DB transaction</li>
</ol>


<h2> Database Connection Pool </h2>
<p> The application uses a database connection pool to manage the database connections. The connection pool is configured in the `src/main/resources/db.properties` file. The connection pool is created using the PoolManager. </p>

<h2> Database Tables </h2>
<p> The database tables are as follows: </p>
<ul>
    <li> `users` </li>
    <table>
        <tr>
            <th> Column Name </th>
            <th> Data Type </th>
            <th> Description </th>
        </tr>
        <tr>
            <td> id </td>
            <td> INT </td>
            <td> User ID </td>
        </tr>
        <tr>
            <td> name </td>
            <td> VARCHAR </td>
            <td> User Name </td>
        </tr>
    </table>
    <li> `seats` </li>
    <table>
        <tr>
            <th> Column Name </th>
            <th> Data Type </th>
            <th> Description </th>
        </tr>
        <tr>
            <td> id </td>
            <td> INT </td>
            <td> Seat ID </td>
        </tr>
       <tr>
            <td> name </td>
            <td> VARCHAR </td>
            <td> Seat Name </td>
        </tr>
        <tr>
            <td> user_id </td>
            <td> INT </td>
            <td> User ID </td>
        </tr>
    </table>
</ul>