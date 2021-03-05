# Package delivery

## How to build

    mvn clean install

or faster

    mvn clean install -Dmaven.test.skip=true

## How to run unit tests

    mvn test

## How to run

    java -jar target/BscTestStanislavSimek-1.0-SNAPSHOT-jar-with-dependencies.jar src/test/resources/initialPackages.txt src/test/resources/initialFees.txt

## Error handling note

In case of bad input data, program just prints errors on error output and usually continues, if it is possible.

## Description

Command line program that keeps a record of packages processed. Each package information consists of weight (in kg) and destination postal code. Think about these packages in the same way, when you send one using postal office. Data are kept in memory.

Program do this:

- read user input from console, user enters line consisting of weight of package and destination postal code
- once per minute - write output to console, each line consists of postal code and total weight of all packages for that postal code
- process user command “quit”, when user enters quit to command line as input, program exits
- take and process command line argument specified at program run – filename of file containing lines in same format as user can enter in command line. This is considered as initial load of package information
- take and process another command line argument specified at program run – filename of file containing information about fees related to package weight. Once such file is specified as argument then output written to console will contain also total fee for packages sent to certain postal code, see format of file and out here after

### Sample input

    3.4 08801
    2 90005
    12.56 08801
    5.5 08079 
    3.2 09300

### Sample output (order by total weight)

    08801 15.960
    08079 5.500
    09300 3.200
    90005 2.000

### Format of file containing fees (sample)

    10 5.00
    5 2.50
    3 2.00
    2 1.50
    1 1.00
    0.5 0.70
    0.2 0.50

Meaning:

- delivery fee of package weighing more than or exactly 10 (kg) is 5.00 (Eur),
- delivery fee of package weighing more than or exactly 5 (kg) and less than 10 (kg) is 2.50 (Eur), etc.

### Sample output (with fees)

    08801 15.960 7.00
    08079 5.500 2.50
    09300 3.200 2.00
    90005 2.000 1.50
