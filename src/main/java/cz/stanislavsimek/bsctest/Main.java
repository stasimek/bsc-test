package cz.stanislavsimek.bsctest;

import cz.stanislavsimek.bsctest.model.Fee;
import cz.stanislavsimek.bsctest.model.Package;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import net.sf.oval.Validator;
import net.sf.oval.ConstraintViolation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class Main {

    /**
     * Loaded packages. Key = postal code.
     */
    private static SortedMap<String, List<Package>> packagesSortedByPostalCode;

    /**
     * Loaded fees. Key = weight.
     */
    private static SortedMap<BigDecimal, Fee> feesSortedByWeightDesc;

    /**
     * Command line program that keeps a record of packages processed. Each package information
     * consists of weight (in kg) and destination postal code. Think about these packages in the
     * same way, when you send one using postal office. Data are kept in memory. 
     *
     * @param args First argument contains optional file name of initial packages. Second argument
     * contains optional file name of initial fees.
     */
    public static void main(String[] args) {
        System.out.println("Program started");

        packagesSortedByPostalCode = new TreeMap<>();
        feesSortedByWeightDesc = new TreeMap<>((Comparator<BigDecimal>) (o1, o2) -> o2.compareTo(o1));

        loadInitialPackagesFromFile(args);
        loadInitialFeesFromFile(args);

        OutputPrinterRunnable outputPrinterRunnable = writeOutputToConsoleOncePerMinute();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                // Read user input from console, consisting of weight of package and destination postal code.
                String inputLine;
                try {
                    inputLine = reader.readLine();
                } catch (IOException ex) {
                    System.err.println("ERROR: " + ex.getMessage());
                    break;
                }
                // When user enters "quit" to command line as input, program exits.
                if ("quit".equals(inputLine)) {
                    outputPrinterRunnable.stop();
                    break;
                }
                if (StringUtils.isNotBlank(inputLine)) {
                    loadPackageLine(inputLine);
                }
            }
        } finally {
            System.out.println("Program finished");
        }
    }

    /**
     * Take and process command line argument specified at program run – filename of file containing
     * lines in same format as user can enter in command line. This is considered as initial load of
     * package information.
     */
    private static void loadInitialPackagesFromFile(String[] args) {
        if (args.length > 0) {
            String initialLoadOfPackagesFileName = args[0];
            File initialLoadOfPackages = new File(initialLoadOfPackagesFileName);
            List<String> lines = null;
            try {
                lines = FileUtils.readLines(initialLoadOfPackages, "US-ASCII");
                System.out.println("Initial packages loaded");
            } catch (IOException e) {
                System.err.println(
                        "ERROR: Unable to load initial packages from file "
                        + initialLoadOfPackagesFileName
                        + " : "
                        + e.getMessage()
                );
            }
            if (lines != null) {
                for (String line : lines) {
                    if (StringUtils.isNotBlank(line)) {
                        loadPackageLine(line);
                    }
                }
            }
        }
    }

    /**
     * Take and process another command line argument specified at program run – filename of file
     * containing information about fees related to package weight. Once such file is specified as
     * argument then output written to console will contain also total fee for packages sent to
     * certain postal code.
     */
    private static void loadInitialFeesFromFile(String[] args) {
        if (args.length > 1) {
            String initialLoadOfFeesFileName = args[1];
            File initialLoadOfFees = new File(initialLoadOfFeesFileName);
            List<String> lines = null;
            try {
                lines = FileUtils.readLines(initialLoadOfFees, "US-ASCII");
                System.out.println("Initial fees loaded");
            } catch (IOException e) {
                System.err.println(
                        "ERROR: Unable to load initial fees from file "
                        + initialLoadOfFeesFileName
                        + " : "
                        + e.getMessage()
                );
            }
            if (lines != null) {
                for (String line : lines) {
                    if (StringUtils.isNotBlank(line)) {
                        loadFeeLine(line);
                    }
                }
            }
        }
    }

    /**
     * Package line format: [weight: positive number, >0, maximal 3 decimal places, . (dot) as
     * decimal separator][space][postal code: fixed 5 digits]
     */
    private static void loadPackageLine(String line) {
        String[] inputParameters = line.split(" ");
        if (inputParameters.length != 2) {
            System.err.println(
                    "ERROR: 2 input parameters expected per package line, get " + inputParameters.length + "."
            );
            return;
        }
        double weightInKg;
        try {
            weightInKg = Double.valueOf(inputParameters[0]);
        } catch (NumberFormatException e) {
            System.err.println(
                    "ERROR: First input parameter for package line should be float, is '" + inputParameters[0] + "'."
            );
            return;
        }
        String postalCode = inputParameters[1];
        Package _package = new Package(weightInKg, postalCode);
        if (isValid(_package)) {
            synchronized (packagesSortedByPostalCode) {
                List<Package> packageList
                        = packagesSortedByPostalCode.getOrDefault(postalCode, new ArrayList<>());
                packageList.add(_package);
                packagesSortedByPostalCode.put(postalCode, packageList);
            }

        }
    }

    private static boolean isValid(Package _package) {
        Validator validator = new Validator();
        List<ConstraintViolation> violations = validator.validate(_package);
        if (!violations.isEmpty()) {
            System.err.println(
                    "ERROR: Package " + _package + " is invalid: " + StringUtils.join(violations, ", ")
            );
            return false;
        }
        return true;
    }

    /**
     * Fee line format: [weight: positive number, >0, maximal 3 decimal places, . (dot) as decimal
     * separator][space][fee: positive number, >=0, fixed two decimals, . (dot) as decimal
     * separator]
     */
    private static void loadFeeLine(String line) {
        String[] inputParameters = line.split(" ");
        if (inputParameters.length != 2) {
            System.err.println(
                    "ERROR: 2 input parameters expected per fee line, get " + inputParameters.length + "."
            );
            return;
        }
        double weightInKg;
        try {
            weightInKg = Double.valueOf(inputParameters[0]);
        } catch (NumberFormatException e) {
            System.err.println(
                    "ERROR: First input parameter for fee line should be float, is '" + inputParameters[0] + "'."
            );
            return;
        }
        double feeValue;
        try {
            feeValue = Double.valueOf(inputParameters[1]);
        } catch (NumberFormatException e) {
            System.err.println(
                    "ERROR: Second input parameter for fee line should be float, is '" + inputParameters[0] + "'."
            );
            return;
        }
        Fee fee = new Fee(weightInKg, feeValue);
        if (isValid(fee)) {
            feesSortedByWeightDesc.put(BigDecimal.valueOf(weightInKg), fee);
        }
    }

    private static boolean isValid(Fee fee) {
        Validator validator = new Validator();
        List<ConstraintViolation> violations = validator.validate(fee);
        if (!violations.isEmpty()) {
            System.err.println(
                    "ERROR: Fee " + fee + " is invalid: " + StringUtils.join(violations, ", ")
            );
            return false;
        }
        return true;
    }

    /**
     * Once per minute - write output to console, each line consists of postal code and total weight
     * of all packages for that postal code
     */
    private static OutputPrinterRunnable writeOutputToConsoleOncePerMinute() {
        OutputPrinterRunnable outputPrinter = new OutputPrinterRunnable();
        Thread thread = new Thread(outputPrinter);
        thread.start();
        return outputPrinter;
    }

    static class OutputPrinterRunnable implements Runnable {

        private volatile boolean exit = false;

        @Override
        public void run() {
            while (!exit) {
                printOutput();
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException ex) {
                    System.err.print(ex.getMessage());
                }
            }
        }

        public void stop() {
            exit = true;
        }
    }

    private static void printOutput() {
        System.out.println("Packages:");
        String output = getOutput();
        if (StringUtils.isNotEmpty(output)) {
            System.out.println(getOutput());
        } else {
            System.out.println("none");
        }
        System.out.println();
    }

    /**
     * Output line format: [postal code: fixed 5 digits][space][total weight: fixed 3 decimal
     * places, . (dot) as decimal separator][space][total fee: fixed 2 decimal places, . (dot) as
     * decimal separator]
     */
    public static String getOutput() {
        SortedMap<BigDecimal, List<String>> outputLinesSortedByTotalWeight
                = new TreeMap<>((Comparator<BigDecimal>) (o1, o2) -> o2.compareTo(o1));
        StringBuilder outputLines = new StringBuilder();
        synchronized (packagesSortedByPostalCode) {
            for (Map.Entry<String, List<Package>> entry : packagesSortedByPostalCode.entrySet()) {
                String postalCode = entry.getKey();
                List<Package> packages = entry.getValue();
                BigDecimal sumOfWeights = new BigDecimal(0);
                for (Package _package : packages) {
                    sumOfWeights = sumOfWeights.add(_package.getWeightInKg());
                }
                String outputLine
                        = postalCode
                        + String.format(" %.3f", sumOfWeights)
                        + getProperFeeString(packages);
                List<String> linesList
                        = outputLinesSortedByTotalWeight.getOrDefault(sumOfWeights, new ArrayList<>());
                linesList.add(outputLine);
                outputLinesSortedByTotalWeight.put(sumOfWeights, linesList);
            }
        }
        for (Map.Entry<BigDecimal, List<String>> entry : outputLinesSortedByTotalWeight.entrySet()) {
            List<String> linesList = entry.getValue();
            for (String line : linesList) {
                outputLines.append(line).append(System.lineSeparator());
            }
        }
        return outputLines.toString().trim();
    }

    private static String getProperFeeString(List<Package> packages) {
        if (!feesSortedByWeightDesc.isEmpty()) {
            BigDecimal properFee = new BigDecimal(0);
            for (Package _package : packages) {
                properFee = properFee.add(getProperFee(_package));
            }
            return String.format(" %.2f", properFee);
        }
        return "";
    }

    /**
     * If fees are: 10 kg => 5.00 Eur, 5 kg => 2.50 Eur, 3 kg => 2.00 Eur
     *
     * - delivery fee of package weighing more than or exactly 10 (kg) is 5.00 (Eur),
     *
     * - delivery fee of package weighing more than or exactly 5 (kg) and less than 10 (kg) is 2.50
     * (Eur), etc.
     */
    private static BigDecimal getProperFee(Package _package) {
        for (Map.Entry<BigDecimal, Fee> entry : feesSortedByWeightDesc.entrySet()) {
            BigDecimal feeWeight = entry.getKey();
            BigDecimal feeValue = entry.getValue().getFee();
            if (_package.getWeightInKg().doubleValue() >= feeWeight.doubleValue()) {
                return feeValue;
            }
        }
        return new BigDecimal(0);
    }

}
